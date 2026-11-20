package seed.seedplusbackend.news;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import seed.seedplusbackend.global.cache.*;
import seed.seedplusbackend.global.error.*;
import seed.seedplusbackend.news.application.*;
import seed.seedplusbackend.news.application.port.*;
import seed.seedplusbackend.news.application.query.NewsSearchQuery;
import seed.seedplusbackend.news.domain.*;
import seed.seedplusbackend.news.infrastructure.NewsJobRunner;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;

@SpringBootTest(
    properties = {
      "news.batch.enabled=false",
      "news.viewer.secret=test-news-secret-longer-than-32-characters",
      "news.viewer.cookie-secure=false"
    })
@AutoConfigureMockMvc
@DisplayName("뉴스 PostgreSQL·HTTP·수집 파이프라인 통합")
class NewsIntegrationTest extends AbstractPostgresContainerTest {
  @Autowired JdbcTemplate jdbc;
  @Autowired MockMvc mvc;
  @Autowired NewsPublicationPort publication;
  @Autowired NewsReadPort read;
  @Autowired NewsOperationsPort operations;
  @Autowired NewsIngestionPort ingestion;
  @Autowired NewsJobLockPort locks;
  @Autowired NewsTelemetryPort telemetry;
  @Autowired NewsCollectionService collection;
  @Autowired NewsMaintenanceService maintenance;
  @Autowired NewsQueryService query;
  @Autowired NewsViewService views;
  @Autowired NewsJobRunner jobs;
  @Autowired CacheStore cache;
  @MockitoBean NewsFeedPort feed;

  @MockitoBean(name = "newsClock")
  Clock clock;

  private OffsetDateTime now;

  @BeforeEach
  void setUp() {
    jdbc.execute(
        "TRUNCATE news_articles,news_ingestion_items,news_article_regions,news_article_industries,news_view_logs,news_digests,news_job_leases,news_source_states RESTART IDENTITY CASCADE");
    jdbc.update("DELETE FROM commercial_estimated_sales WHERE trdar_cd='NEWS_TEST'");
    jdbc.update(
        "DELETE FROM reb_small_retail_rent_metrics WHERE reb_small_retail_rent_area_id IN(SELECT reb_small_retail_rent_area_id FROM reb_small_retail_rent_areas WHERE source_area_key='NEWS_TEST')");
    jdbc.update("DELETE FROM reb_small_retail_rent_areas WHERE source_area_key='NEWS_TEST'");
    setTime("2026-11-20T12:00:00+09:00");
    cache.evict(CacheSpec.NEWS_POPULAR, "2026-11-20");
    cache.evict(CacheSpec.NEWS_POPULAR, "2026-11-21");
  }

  private void setTime(String value) {
    now = OffsetDateTime.parse(value);
    Clock fixed = Clock.fixed(now.toInstant(), ZoneOffset.UTC);
    when(clock.instant()).thenReturn(fixed.instant());
    when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    when(clock.withZone(NewsPolicy.ZONE)).thenReturn(fixed.withZone(NewsPolicy.ZONE));
  }

  private NewsItem news(
      int id,
      String title,
      OffsetDateTime published,
      NewsCategory category,
      List<Long> regions,
      List<Long> industries) {
    return new NewsItem(
        NewsSource.MSS_PRESS,
        "86:" + id,
        title,
        null,
        null,
        "https://www.mss.go.kr/site/smba/ex/bbs/View.do?cbIdx=86&bcIdx=" + id,
        published,
        category,
        "창업",
        regions,
        industries);
  }

  private long publish(int id) {
    return publication.publish(
        news(
            id,
            "소상공인 창업 " + id,
            now.minusHours(1),
            NewsCategory.POLICY_FINANCE,
            List.of(),
            List.of()));
  }

  private NewsFeedPort.RawItem raw(int id) {
    return new NewsFeedPort.RawItem(
        "소상공인 지원 " + id,
        "https://www.mss.go.kr/site/smba/ex/bbs/View.do?cbIdx=86&bcIdx=" + id,
        "20261120090000");
  }

  private long count(String table) {
    return jdbc.queryForObject("SELECT count(*) FROM " + table, Long.class);
  }

  private String state() {
    return jdbc.queryForObject(
        "SELECT status FROM news_ingestion_items ORDER BY item_id LIMIT 1", String.class);
  }

  @Test
  @DisplayName("비회원 뉴스 조회는 공개하고 상세 GET은 집계하지 않으며 기존 쓰기 권한은 유지한다")
  void publicHttpAndViews() throws Exception {
    long id = publish(1);
    mvc.perform(get("/api/v1/news/articles"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content[0].id").value(id))
        .andExpect(jsonPath("$.data.pageInfo.size").value(20))
        .andExpect(jsonPath("$.data.content[0].displayMode").value("OUTLINK"))
        .andExpect(jsonPath("$.data.content[0].body").isEmpty());
    mvc.perform(get("/api/v1/news/articles/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.viewCount").value(0));
    var response =
        mvc.perform(post("/api/v1/news/articles/" + id + "/views"))
            .andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", "no-store"))
            .andReturn()
            .getResponse();
    String value =
        response.getHeader("Set-Cookie").split(";", 2)[0].substring("seed_news_viewer=".length());
    mvc.perform(
            post("/api/v1/news/articles/" + id + "/views")
                .cookie(new Cookie("seed_news_viewer", value)))
        .andExpect(status().isOk());
    assertThat(query.detail(id).viewCount()).isEqualTo(1);
    mvc.perform(
            post("/api/v1/community/posts")
                .contentType("application/json")
                .content("{\"title\":\"x\",\"content\":\"y\"}"))
        .andExpect(status().isUnauthorized());
    mvc.perform(get("/api/v1/news/articles/999999")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("페이지 크기·기간·카테고리·정렬 오류를 400으로 응답한다")
  void invalidSearch() throws Exception {
    for (String param :
        List.of(
            "size=101",
            "size=0",
            "page=-1",
            "category=WRONG",
            "sort=title,asc",
            "from=2026-11-21&to=2026-11-20")) {
      mvc.perform(get("/api/v1/news/articles?" + param)).andExpect(status().isBadRequest());
    }
  }

  @Test
  @DisplayName("복합 필터·KST 날짜 경계·동률 ID 정렬·상위 업종 검색을 적용한다")
  void searchAndPaging() throws Exception {
    Long gangnam =
        jdbc.queryForObject("SELECT region_id FROM regions WHERE code='11680'", Long.class);
    Long songpa =
        jdbc.queryForObject("SELECT region_id FROM regions WHERE code='11710'", Long.class);
    Long industry =
        jdbc.queryForObject(
            "SELECT industry_id FROM industries WHERE industry_code='I212'", Long.class);
    Long parent =
        jdbc.queryForObject(
            "SELECT parent_industry_id FROM industries WHERE industry_id=?", Long.class, industry);
    var first =
        news(
            1,
            "소상공인 100% 성장",
            OffsetDateTime.parse("2026-11-19T15:00:00Z"),
            NewsCategory.POLICY_FINANCE,
            List.of(gangnam),
            List.of(industry));
    long firstId = publication.publish(first);
    long lastId =
        publication.publish(
            news(
                2,
                "소상공인 성장",
                first.publishedAt(),
                first.category(),
                List.of(gangnam),
                List.of(industry)));
    publication.publish(
        news(
            3,
            "소상공인 성장",
            first.publishedAt().minusSeconds(1),
            first.category(),
            List.of(gangnam),
            List.of(industry)));
    publication.publish(
        news(
            4,
            "소상공인 성장",
            first.publishedAt(),
            first.category(),
            List.of(songpa),
            List.of(industry)));
    var filter =
        new NewsSearchQuery(
            "POLICY_FINANCE",
            null,
            List.of(gangnam),
            List.of(parent),
            LocalDate.parse("2026-11-20"),
            LocalDate.parse("2026-11-20"),
            PageRequest.of(0, 1));
    var result = query.search(filter);
    assertThat(result.getTotalElements()).isEqualTo(2);
    assertThat(result.getContent().getFirst().id()).isEqualTo(lastId);
    assertThat(
            query
                .search(
                    new NewsSearchQuery(
                        null, "%", List.of(), List.of(), null, null, PageRequest.of(0, 20)))
                .getContent())
        .extracting(x -> x.id())
        .containsExactly(firstId);
    assertThat(
            query
                .search(
                    new NewsSearchQuery(
                        null,
                        "강남구",
                        List.of(gangnam),
                        List.of(industry),
                        filter.from(),
                        filter.to(),
                        PageRequest.of(1, 1)))
                .getContent())
        .extracting(x -> x.id())
        .containsExactly(firstId);
  }

  @Test
  @DisplayName("조회 동시 요청은 하루 한 번만 증가하고 한국 시간 자정에 새 집계를 시작한다")
  void concurrentDailyViews() throws Exception {
    setTime("2026-11-20T23:59:59+09:00");
    long id = publish(1);
    try (var executor = Executors.newFixedThreadPool(8)) {
      var start = new CountDownLatch(1);
      var futures = new ArrayList<Future<?>>();
      for (int i = 0; i < 16; i++)
        futures.add(
            executor.submit(
                () -> {
                  start.await();
                  views.record(id, 777L, null);
                  return null;
                }));
      start.countDown();
      for (var future : futures) future.get(20, TimeUnit.SECONDS);
    }
    assertThat(query.detail(id).viewCount()).isEqualTo(1);
    assertThat(count("news_view_logs")).isEqualTo(1);
    setTime("2026-11-21T00:00:00+09:00");
    views.record(id, 777L, null);
    assertThat(query.detail(id).viewCount()).isEqualTo(2);
  }

  @Test
  @DisplayName("최근 7일만 TOP3에 반영하고 조회 없는 뉴스는 최신순으로 채운다")
  void popularity() {
    long a = publish(1), b = publish(2), c = publish(3), d = publish(4);
    LocalDate date = LocalDate.parse("2026-11-20");
    operations.recordView(a, "a", date.minusDays(6), now);
    operations.recordView(b, "b", date, now);
    for (int i = 0; i < 5; i++) operations.recordView(d, "expired-" + i, date.minusDays(7), now);
    assertThat(query.popular().getContent()).extracting(x -> x.id()).containsExactly(b, a, d);
    operations.pruneViews(date.minusDays(6));
    assertThat(query.detail(d).viewCount()).isEqualTo(5);
    assertThat(count("news_view_logs")).isEqualTo(2);
  }

  @Test
  @DisplayName("DB도 RSS 본문 저장을 거부한다")
  void dbContentGuard() {
    long id = publish(1);
    assertThatThrownBy(
            () -> jdbc.update("UPDATE news_articles SET body='금지된 본문' WHERE news_article_id=?", id))
        .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    assertThat(query.detail(id).body()).isNull();
  }

  @Test
  @DisplayName("재수집·잘못된 항목·무관한 항목을 격리하고 기사 수정 시 조회수를 보존한다")
  void collectionIdempotency() {
    when(feed.fetch(NewsSource.MSS_PRESS))
        .thenReturn(
            List.of(
                raw(1),
                raw(2),
                new NewsFeedPort.RawItem("소상공인 잘못된 날짜", raw(3).url(), "bad"),
                new NewsFeedPort.RawItem("인사 발령", raw(4).url(), raw(4).publishedText())));
    collection.collect(NewsSource.MSS_PRESS);
    collection.collect(NewsSource.MSS_PRESS);
    assertThat(count("news_articles")).isEqualTo(2);
    assertThat(count("news_ingestion_items")).isEqualTo(4);
    assertThat(
            jdbc.queryForObject(
                "SELECT count(*) FROM news_ingestion_items WHERE status='INVALID'", Long.class))
        .isEqualTo(1);
    assertThat(
            jdbc.queryForObject(
                "SELECT count(*) FROM news_ingestion_items WHERE status='SKIPPED'", Long.class))
        .isEqualTo(1);
    long id =
        jdbc.queryForObject(
            "SELECT news_article_id FROM news_articles WHERE source_key='86:1'", Long.class);
    views.record(id, 5L, null);
    when(feed.fetch(NewsSource.MSS_PRESS))
        .thenReturn(
            List.of(new NewsFeedPort.RawItem("강남구 소상공인 수정", raw(1).url(), raw(1).publishedText())));
    collection.collect(NewsSource.MSS_PRESS);
    assertThat(query.detail(id).viewCount()).isEqualTo(1);
    assertThat(query.detail(id).title()).contains("수정");
    assertThat(query.detail(id).regions()).extracting(x -> x.name()).contains("강남구");
  }

  @Test
  @DisplayName("수집 소스 장애 시 기존 기사를 유지하고 성공 기록을 덮어쓰지 않는다")
  void sourceFailure() {
    when(feed.fetch(NewsSource.MSS_PRESS)).thenReturn(List.of(raw(1)));
    collection.collect(NewsSource.MSS_PRESS);
    var last =
        jdbc.queryForObject(
            "SELECT last_success_at FROM news_source_states WHERE source='MSS_PRESS'",
            OffsetDateTime.class);
    when(feed.fetch(NewsSource.MSS_PRESS))
        .thenThrow(new ApplicationException(ErrorCode.NEWS_SOURCE_FAILED));
    assertThatThrownBy(() -> collection.collect(NewsSource.MSS_PRESS))
        .isInstanceOf(ApplicationException.class);
    assertThat(count("news_articles")).isEqualTo(1);
    assertThat(
            jdbc.queryForObject(
                "SELECT last_success_at FROM news_source_states WHERE source='MSS_PRESS'",
                OffsetDateTime.class))
        .isEqualTo(last);
    assertThat(count("news_job_leases")).isZero();
  }

  @Test
  @DisplayName("동일 소스 동시 실행은 한 번만 가져온다")
  void concurrentSource() throws Exception {
    var entered = new CountDownLatch(1);
    var proceed = new CountDownLatch(1);
    when(feed.fetch(NewsSource.MSS_PRESS))
        .thenAnswer(
            invocation -> {
              entered.countDown();
              assertThat(proceed.await(10, TimeUnit.SECONDS)).isTrue();
              return List.of(raw(1));
            });
    try (var executor = Executors.newFixedThreadPool(2)) {
      var first = executor.submit(() -> collection.collect(NewsSource.MSS_PRESS));
      assertThat(entered.await(10, TimeUnit.SECONDS)).isTrue();
      var second = executor.submit(() -> collection.collect(NewsSource.MSS_PRESS));
      second.get(10, TimeUnit.SECONDS);
      proceed.countDown();
      first.get(10, TimeUnit.SECONDS);
    }
    verify(feed, times(1)).fetch(NewsSource.MSS_PRESS);
    assertThat(count("news_articles")).isEqualTo(1);
  }

  @Test
  @DisplayName("제목이 같은 다른 기사는 병합하지 않고 후보만 기록한다")
  void duplicateCandidate() {
    when(feed.fetch(NewsSource.MSS_PRESS))
        .thenReturn(
            List.of(
                raw(1),
                new NewsFeedPort.RawItem(raw(1).title(), raw(2).url(), raw(2).publishedText())));
    collection.collect(NewsSource.MSS_PRESS);
    assertThat(count("news_articles")).isEqualTo(2);
    assertThat(
            jdbc.queryForObject(
                "SELECT duplicate_candidate_id FROM news_ingestion_items WHERE item_id=2",
                Long.class))
        .isNotNull();
  }

  @Test
  @DisplayName("반영 실패 후 재시도는 회복하고 네 번 실패한 항목은 무한 재시도하지 않는다")
  void retryAndDeadLetter() {
    when(feed.fetch(NewsSource.MSS_PRESS)).thenReturn(List.of(raw(1)));
    AtomicInteger failures = new AtomicInteger(1);
    NewsPublicationPort failing =
        new NewsPublicationPort() {
          public long publish(NewsItem item) {
            if (failures.getAndDecrement() > 0)
              throw new ApplicationException(ErrorCode.INTERNAL_SERVER_ERROR);
            return publication.publish(item);
          }

          public void hide(NewsSource source, String key) {
            publication.hide(source, key);
          }
        };
    var service = new NewsCollectionService(feed, ingestion, failing, locks, telemetry, clock);
    service.collect(NewsSource.MSS_PRESS);
    assertThat(state()).isEqualTo("RETRY");
    jdbc.update("UPDATE news_ingestion_items SET next_attempt_at=now()");
    service.collect(NewsSource.MSS_PRESS);
    assertThat(state()).isEqualTo("PUBLISHED");
    assertThat(count("news_articles")).isEqualTo(1);
    failures.set(100);
    when(feed.fetch(NewsSource.MSS_PRESS)).thenReturn(List.of(raw(2)));
    for (int i = 0; i < 5; i++) {
      jdbc.update("UPDATE news_ingestion_items SET next_attempt_at=now()");
      service.collect(NewsSource.MSS_PRESS);
    }
    assertThat(
            jdbc.queryForObject(
                "SELECT attempts FROM news_ingestion_items WHERE original_url=?",
                Integer.class,
                raw(2).url()))
        .isEqualTo(4);
    assertThat(
            jdbc.queryForObject(
                "SELECT status FROM news_ingestion_items WHERE original_url=?",
                String.class,
                raw(2).url()))
        .isEqualTo("FAILED");
  }

  @Test
  @DisplayName("만료된 실행은 새 실행의 기사 반영과 잠금 해제를 침범하지 않는다")
  void leaseFencing() {
    UUID old = UUID.randomUUID(), fresh = UUID.randomUUID();
    assertThat(locks.acquire("MSS_PRESS", old)).isTrue();
    assertThat(locks.acquire("MSS_PRESS", fresh)).isFalse();
    jdbc.update("UPDATE news_job_leases SET lease_until=clock_timestamp()-interval '1 second'");
    assertThat(locks.acquire("MSS_PRESS", fresh)).isTrue();
    assertThatThrownBy(() -> locks.owned("MSS_PRESS", old, () -> publish(1)))
        .isInstanceOf(ApplicationException.class);
    locks.release("MSS_PRESS", old);
    locks.owned("MSS_PRESS", fresh, () -> publish(1));
    assertThat(count("news_articles")).isEqualTo(1);
  }

  @Test
  @DisplayName("Spring Batch는 실제 수집 실행 이력을 남긴다")
  void batchHistory() {
    when(feed.fetch(NewsSource.MSS_PRESS)).thenReturn(List.of(raw(1)));
    var execution = jobs.run("MSS_PRESS");
    assertThat(execution.getStatus())
        .isEqualTo(org.springframework.batch.core.BatchStatus.COMPLETED);
    assertThat(
            jdbc.queryForObject(
                "SELECT status FROM batch_job_execution WHERE job_execution_id=?",
                String.class,
                execution.getId()))
        .isEqualTo("COMPLETED");
  }

  @Test
  @DisplayName("다이제스트는 전일 기사만 카테고리별 최대 3개를 안정적으로 교체한다")
  void digest() {
    var yesterday = now.minusDays(1);
    for (int i = 1; i <= 4; i++)
      publication.publish(
          news(i, "소상공인 " + i, yesterday, NewsCategory.POLICY_FINANCE, List.of(), List.of()));
    long popular =
        jdbc.queryForObject(
            "SELECT news_article_id FROM news_articles WHERE source_key='86:1'", Long.class);
    operations.recordView(popular, "a", yesterday.toLocalDate(), yesterday);
    publish(5);
    maintenance.digest(now.toLocalDate());
    maintenance.digest(now.toLocalDate());
    assertThat(count("news_digests")).isEqualTo(3);
    assertThat(query.digest(now.toLocalDate()).getContent())
        .extracting(x -> x.id())
        .containsExactly(popular, 4L, 3L);
    assertThat(query.digest(now.toLocalDate().minusDays(1))).isEmpty();
  }

  @Test
  @DisplayName("분기 매출과 임대료 비교는 실제 테이블을 읽고 정정·재실행 때 기사와 조회수를 보존한다")
  void insights() {
    sales("20254", 100);
    sales("20261", 120);
    rent(2025, 4, "0");
    rent(2026, 1, "10");
    maintenance.insights();
    assertThat(
            jdbc.queryForObject(
                "SELECT count(*) FROM news_articles WHERE source IN('SEOUL_SALES','REB_RENT')",
                Long.class))
        .isEqualTo(2);
    long salesId =
        jdbc.queryForObject(
            "SELECT news_article_id FROM news_articles WHERE source='SEOUL_SALES'", Long.class);
    assertThat(query.detail(salesId).body()).contains("2025년 4분기 100", "20.00%");
    views.record(salesId, 5L, null);
    jdbc.update(
        "UPDATE commercial_estimated_sales SET thsmon_selng_amt=150 WHERE trdar_cd='NEWS_TEST' AND stdr_yyqu_cd='20261'");
    maintenance.insights();
    assertThat(query.detail(salesId).body()).contains("50.00%");
    assertThat(query.detail(salesId).viewCount()).isEqualTo(1);
    assertThat(count("news_articles")).isEqualTo(2);
    jdbc.update(
        "UPDATE commercial_estimated_sales SET thsmon_selng_amt=100 WHERE trdar_cd='NEWS_TEST' AND stdr_yyqu_cd='20261'");
    maintenance.insights();
    assertThatThrownBy(() -> query.detail(salesId)).isInstanceOf(ApplicationException.class);
  }

  @Test
  @DisplayName("중간 분기 누락은 비교하지 않고 원천 유실 시 마지막 성공 카드를 유지한다")
  void insightMissingPeriod() {
    sales("20254", 100);
    sales("20262", 120);
    maintenance.insights();
    assertThat(count("news_articles")).isZero();
    sales("20261", 110);
    maintenance.insights();
    assertThat(count("news_articles")).isEqualTo(1);
    jdbc.update(
        "DELETE FROM commercial_estimated_sales WHERE trdar_cd='NEWS_TEST' AND stdr_yyqu_cd='20262'");
    maintenance.insights();
    assertThat(
            jdbc.queryForObject(
                "SELECT active FROM news_articles WHERE source_key='NEWS_TEST:NEWS_INDUSTRY:20262'",
                Boolean.class))
        .isTrue();
  }

  @Test
  @DisplayName("소스 장애 중에도 DB에 남은 100개 초과 항목을 모두 복구한다")
  void recoverPersistedBacklog() {
    ingestion.stage(
        NewsSource.MSS_PRESS,
        java.util.stream.IntStream.rangeClosed(1, 105).mapToObj(this::raw).toList());
    when(feed.fetch(NewsSource.MSS_PRESS))
        .thenThrow(new ApplicationException(ErrorCode.NEWS_SOURCE_FAILED));
    assertThatThrownBy(() -> collection.collect(NewsSource.MSS_PRESS))
        .isInstanceOf(ApplicationException.class);
    assertThat(count("news_articles")).isEqualTo(105);
    assertThat(
            jdbc.queryForObject(
                "SELECT count(*) FROM news_ingestion_items WHERE status='PUBLISHED'", Long.class))
        .isEqualTo(105);
    assertThat(count("news_job_leases")).isZero();
  }

  private void sales(String period, int amount) {
    jdbc.update(
        """
     INSERT INTO commercial_estimated_sales(stdr_yyqu_cd,trdar_cd,trdar_cd_nm,svc_induty_cd,svc_induty_cd_nm,thsmon_selng_amt)
     VALUES (?,'NEWS_TEST','테스트상권','NEWS_INDUSTRY','카페',?)
     """,
        period,
        amount);
  }

  private void rent(int year, int quarter, String amount) {
    jdbc.update(
        """
     INSERT INTO reb_small_retail_rent_areas(source_area_key,source_row_number,area_name,area_path,area_level)
     VALUES ('NEWS_TEST',1,'테스트지역','테스트지역',1) ON CONFLICT DO NOTHING
     """);
    jdbc.update(
        """
     INSERT INTO reb_small_retail_rent_metrics(reb_small_retail_rent_area_id,reference_year,reference_quarter,
       rent_per_square_meter_thousand_krw,source_file_name,source_file_hash)
     SELECT reb_small_retail_rent_area_id,?,?,?,'fixture.csv','fixture' FROM reb_small_retail_rent_areas WHERE source_area_key='NEWS_TEST'
     """,
        year,
        quarter,
        new BigDecimal(amount));
  }
}
