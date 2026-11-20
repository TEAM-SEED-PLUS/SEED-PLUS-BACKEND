package seed.seedplusbackend.news.domain;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import seed.seedplusbackend.global.error.DomainException;

@DisplayName("뉴스 정규화·이용 정책·인사이트")
class NewsPolicyTest {
  private final OffsetDateTime now = OffsetDateTime.parse("2026-11-20T12:00:00+09:00");

  @Test
  @DisplayName("게시판 id 대신 URL 기사 번호로 식별하고 KST 날짜를 파싱한다")
  void canonicalIdentity() {
    var a =
        RssNewsNormalizer.normalize(
            NewsSource.MSS_PRESS,
            "소상공인 지원",
            "https://mss.go.kr/site/smba/ex/bbs/View.do?bcIdx=100&cbIdx=86&utm_source=test#top",
            "20261120090000",
            now);
    assertThat(a.sourceKey()).isEqualTo("86:100");
    assertThat(a.originalUrl())
        .isEqualTo("https://www.mss.go.kr/site/smba/ex/bbs/View.do?cbIdx=86&bcIdx=100");
    assertThat(a.publishedAt().toInstant()).isEqualTo(Instant.parse("2026-11-20T00:00:00Z"));
    assertThat(a.summary()).isNull();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "http://www.mss.go.kr/site/smba/ex/bbs/View.do?cbIdx=86&bcIdx=1",
        "https://evil.test/site/smba/ex/bbs/View.do?cbIdx=86&bcIdx=1",
        "https://www.mss.go.kr/site/smba/ex/bbs/View.do?cbIdx=310&bcIdx=1",
        "https://www.mss.go.kr/site/smba/ex/bbs/View.do?cbIdx=86&bcIdx=1&bcIdx=2"
      })
  @DisplayName("잘못된 출처·게시판·중복 키 URL을 거부한다")
  void invalidUrl(String url) {
    assertThatThrownBy(
            () ->
                RssNewsNormalizer.normalize(NewsSource.MSS_PRESS, "창업", url, "20261120090000", now))
        .isInstanceOf(DomainException.class);
  }

  @ParameterizedTest
  @ValueSource(strings = {"20260230090000", "202611", "20270101000000"})
  @DisplayName("잘못된 날짜와 미래 게시일을 거부한다")
  void invalidDate(String date) {
    assertThatThrownBy(
            () ->
                RssNewsNormalizer.normalize(
                    NewsSource.MSS_PRESS,
                    "창업",
                    "https://www.mss.go.kr/site/smba/ex/bbs/View.do?cbIdx=86&bcIdx=1",
                    date,
                    now))
        .isInstanceOf(DomainException.class);
  }

  @Test
  @DisplayName("RSS 본문과 요약은 도메인에서 저장을 차단한다")
  void disallowBody() {
    assertThatThrownBy(
            () ->
                new NewsItem(
                    NewsSource.MSS_PRESS,
                    "86:1",
                    "창업",
                    "요약",
                    "본문",
                    "https://www.mss.go.kr/",
                    now,
                    NewsCategory.POLICY_FINANCE,
                    "",
                    List.of(),
                    List.of()))
        .isInstanceOf(DomainException.class);
  }

  @Test
  @DisplayName("관련성 사전은 버전과 함께 관리하고 모르는 지역을 추정하지 않는다")
  void relevance() {
    assertThat(NewsPolicy.RULE_VERSION).isEqualTo("v1");
    assertThat(NewsPolicy.tags("강남구 소상공인 창업 지원")).containsExactly("소상공인", "창업");
    assertThat(NewsPolicy.relevant("인사 발령")).isFalse();
  }

  @Test
  @DisplayName("1분기는 전년도 4분기와 비교하고 원인 추정 없이 수치를 표시한다")
  void quarterBoundary() {
    var c =
        new InsightComparison(
            NewsSource.SEOUL_SALES,
            "area:industry:20261",
            "강남 카페",
            2026,
            1,
            new BigDecimal("120"),
            new BigDecimal("100"),
            List.of(),
            List.of());
    var item = c.toItem(now);
    assertThat(item.body()).contains("2025년 4분기 100", "20.00%", "차이: 20");
    assertThat(item.displayMode()).isEqualTo("INTERNAL");
  }

  @Test
  @DisplayName("이전 값 0은 증감률을 만들지 않고 미변경·누락은 카드를 생성하지 않는다")
  void missingAndZero() {
    var zero =
        new InsightComparison(
            NewsSource.REB_RENT,
            "k",
            "지역",
            2026,
            1,
            BigDecimal.TEN,
            BigDecimal.ZERO,
            List.of(),
            List.of());
    assertThat(zero.toItem(now).summary()).contains("증감률은 계산하지 않습니다").doesNotContain("%");
    assertThat(
            new InsightComparison(
                    NewsSource.REB_RENT,
                    "k",
                    "지역",
                    2026,
                    1,
                    BigDecimal.TEN,
                    null,
                    List.of(),
                    List.of())
                .changed())
        .isFalse();
    assertThat(
            new InsightComparison(
                    NewsSource.REB_RENT,
                    "k",
                    "지역",
                    2026,
                    1,
                    BigDecimal.TEN,
                    BigDecimal.TEN,
                    List.of(),
                    List.of())
                .changed())
        .isFalse();
  }
}
