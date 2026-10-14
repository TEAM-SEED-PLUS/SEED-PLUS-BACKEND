package seed.seedplusbackend.community;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import seed.seedplusbackend.community.application.*;
import seed.seedplusbackend.community.application.command.*;
import seed.seedplusbackend.community.application.query.*;
import seed.seedplusbackend.community.application.result.*;
import seed.seedplusbackend.community.infrastructure.repository.*;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.security.AuthenticatedUser;
import seed.seedplusbackend.industry.domain.entity.*;
import seed.seedplusbackend.industry.infrastructure.repository.IndustryJpaRepository;
import seed.seedplusbackend.region.infrastructure.repository.RegionJpaRepository;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.fixture.*;
import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.infrastructure.repository.UserJpaRepository;

@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
@DisplayName("커뮤니티 PostgreSQL·HTTP 통합")
class CommunityIntegrationTest extends AbstractPostgresContainerTest {
  @Autowired CommunityCommandService commands;
  @Autowired CommunityQueryService queries;
  @Autowired CommunityPostJpaRepository posts;
  @Autowired CommunityCommentJpaRepository comments;
  @Autowired UserJpaRepository users;
  @Autowired RegionJpaRepository regions;
  @Autowired IndustryJpaRepository industries;
  @Autowired JdbcTemplate jdbc;
  @Autowired MockMvc mvc;
  @Autowired PlatformTransactionManager transactionManager;
  @Autowired jakarta.persistence.EntityManagerFactory entityManagerFactory;
  private User owner;
  private User other;
  private Long postId;
  private Long regionId;
  private Long secondRegionId;
  private Long industryId;
  private String key;
  private static final String PATH = "/api/v1/community/posts";

  @BeforeEach
  void setUp() {
    jdbc.update("delete from community_post_regions");
    jdbc.update("delete from community_post_industries");
    jdbc.update("delete from community_likes");
    jdbc.update("delete from community_comments");
    jdbc.update("delete from community_posts");
    key = UUID.randomUUID().toString().substring(0, 8);
    owner = users.saveAndFlush(UserFixture.generalActiveUser("owner" + key));
    other = users.saveAndFlush(UserFixture.generalActiveUser("other" + key));
    regionId = regions.findByCode("11680").orElseThrow().getId();
    secondRegionId = regions.findByCode("11710").orElseThrow().getId();
    industryId = industries.saveAndFlush(IndustryFixture.largeRoot("CM" + key, "테스트업종")).getId();
    postId = commands.create(owner.getId(), save("첫 글", "첫 본문", List.of(), List.of())).postId();
  }

  private SaveCommunityPostCommand save(
      String title, String content, List<Long> regionTags, List<Long> industryTags) {
    return new SaveCommunityPostCommand(title, content, null, regionTags, industryTags);
  }

  private AuthenticatedUser principal(User u) {
    return new AuthenticatedUser(u.getId(), u.getPhoneNumber(), u.getRole());
  }

  private CommunityPostSearchQuery search(
      String keyword, List<Long> regionTags, List<Long> industryTags, int size) {
    return new CommunityPostSearchQuery(
        keyword,
        regionTags,
        industryTags,
        PageRequest.of(0, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))));
  }

  private int count(String table) {
    return jdbc.queryForObject("select count(*) from " + table, Integer.class);
  }

  @Test
  @DisplayName("비회원 글·댓글 조회는 공개되고 응답에 개인 정보가 섞이지 않는다")
  void anonymousReadsReturnSafeResponses() throws Exception {
    commands.createComment(owner.getId(), postId, new CreateCommunityCommentCommand("댓글"));
    mvc.perform(get(PATH))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.pageInfo.page").value(0))
        .andExpect(jsonPath("$.data.pageInfo.size").value(20))
        .andExpect(jsonPath("$.data.content[0].liked").value(false))
        .andExpect(jsonPath("$.data.content[0].isOwner").value(false))
        .andExpect(jsonPath("$.data.content[0].author.email").doesNotExist());
    mvc.perform(get(PATH + "/" + postId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content").value("첫 본문"));
    mvc.perform(get(PATH + "/" + postId + "/comments"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content[0].content").value("댓글"))
        .andExpect(jsonPath("$.data.content[0].isOwner").value(false));
  }

  @Test
  @DisplayName("모든 쓰기 및 좋아요 경로는 미인증 요청을 차단한다")
  void writesRequireAuthentication() throws Exception {
    mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnauthorized());
    mvc.perform(patch(PATH + "/" + postId).contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnauthorized());
    mvc.perform(delete(PATH + "/" + postId)).andExpect(status().isUnauthorized());
    mvc.perform(
            post(PATH + "/" + postId + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isUnauthorized());
    mvc.perform(delete(PATH + "/" + postId + "/comments/1")).andExpect(status().isUnauthorized());
    mvc.perform(post(PATH + "/" + postId + "/likes")).andExpect(status().isUnauthorized());
    mvc.perform(delete(PATH + "/" + postId + "/likes")).andExpect(status().isUnauthorized());
    mvc.perform(get(PATH + "/" + postId + "/likes")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("공개 경로라도 유효하지 않은 JWT는 401이다")
  void publicReadRejectsInvalidJwt() throws Exception {
    mvc.perform(get(PATH).header("Authorization", "Bearer invalid.token"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(3002));
  }

  @Test
  @DisplayName("인증한 본인의 글 작성·수정은 201·200이며 빈 태그 배열은 태그를 제거한다")
  void createAndUpdateHttpContract() throws Exception {
    mvc.perform(
            post(PATH)
                .with(user(principal(owner)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"새 글\",\"content\":\"본문\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value(201))
        .andExpect(jsonPath("$.data.isOwner").value(true));
    commands.update(
        owner.getId(), postId, save("제목", "내용", List.of(regionId), List.of(industryId)));
    mvc.perform(
            patch(PATH + "/" + postId)
                .with(user(principal(owner)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"title\":\"수정\",\"content\":\"변경\",\"regionIds\":[],\"industryIds\":[]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.regionTags").isEmpty())
        .andExpect(jsonPath("$.data.industryTags").isEmpty())
        .andExpect(jsonPath("$.data.updatedAt").isNotEmpty());
    assertThat(count("community_post_regions")).isZero();
    assertThat(count("community_post_industries")).isZero();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "{\"title\":\" \",\"content\":\"본문\"}",
        "{\"title\":\"제목\",\"content\":\" \"}",
        "{\"title\":\"제목\",\"content\":\"본문\",\"regionIds\":[1,1]}",
        "{\"title\":\"제목\",\"content\":\"본문\",\"regionIds\":[1,2,3]}",
        "{\"title\":\"제목\",\"content\":\"본문\",\"industryIds\":[null]}",
        "{\"title\":\"제목\",\"content\":\"본문\",\"industryIds\":[-1]}"
      })
  @DisplayName("잘못된 글 입력은 400이며 저장하지 않는다")
  void rejectsInvalidRequest(String body) throws Exception {
    mvc.perform(
            post(PATH)
                .with(user(principal(owner)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
    assertThat(count("community_posts")).isEqualTo(1);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "page=-1",
        "size=0",
        "size=101",
        "sort=content,asc",
        "regionIds=0",
        "regionIds=999999999",
        "industryIds=999999999",
        "regionIds=1,1"
      })
  @DisplayName("잘못된 검색 파라미터는 400이다")
  void rejectsInvalidSearch(String param) throws Exception {
    String[] parts = param.split("=", 2);
    mvc.perform(get(PATH).param(parts[0], parts[1])).andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("법정동·없는 지역·비활성 업종을 쓰기와 필터에서 거부한다")
  void validatesReferenceTags() throws Exception {
    Long dong = regions.findByCode("1168010100").orElseThrow().getId();
    Long inactive =
        industries
            .saveAndFlush(
                Industry.builder()
                    .industryCode("IN" + key)
                    .name("중단")
                    .level(IndustryLevel.LARGE)
                    .status(IndustryStatus.INACTIVE)
                    .build())
            .getId();
    for (String body :
        List.of(
            "{\"title\":\"제목\",\"content\":\"본문\",\"regionIds\":[" + dong + "]}",
            "{\"title\":\"제목\",\"content\":\"본문\",\"regionIds\":[999999999]}",
            "{\"title\":\"제목\",\"content\":\"본문\",\"industryIds\":[" + inactive + "]}")) {
      mvc.perform(
              post(PATH)
                  .with(user(principal(owner)))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(body))
          .andExpect(status().isBadRequest());
    }
    mvc.perform(get(PATH).param("regionIds", dong.toString())).andExpect(status().isBadRequest());
    mvc.perform(get(PATH).param("industryIds", inactive.toString()))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("타인 글 수정·삭제와 타인 댓글 삭제를 차단한다")
  void enforcesOwnershipAndCommentScope() throws Exception {
    mvc.perform(
            patch(PATH + "/" + postId)
                .with(user(principal(other)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"수정\",\"content\":\"본문\"}"))
        .andExpect(status().isForbidden());
    mvc.perform(delete(PATH + "/" + postId).with(user(principal(other))))
        .andExpect(status().isForbidden());
    Long commentId =
        commands
            .createComment(owner.getId(), postId, new CreateCommunityCommentCommand("댓글"))
            .commentId();
    mvc.perform(delete(PATH + "/" + postId + "/comments/" + commentId).with(user(principal(other))))
        .andExpect(status().isForbidden());
    Long second = commands.create(owner.getId(), save("다른 글", "본문", List.of(), List.of())).postId();
    mvc.perform(delete(PATH + "/" + second + "/comments/" + commentId).with(user(principal(owner))))
        .andExpect(status().isNotFound());
    assertThat(queries.getPost(postId, owner.getId()).commentCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("댓글 삭제는 기록을 보존하고 목록·카운트에서 제외한다")
  void softDeletesComments() throws Exception {
    Long first =
        commands
            .createComment(owner.getId(), postId, new CreateCommunityCommentCommand("첫 댓글"))
            .commentId();
    Long second =
        commands
            .createComment(other.getId(), postId, new CreateCommunityCommentCommand("둘째 댓글"))
            .commentId();
    assertThat(
            queries.getComments(
                postId,
                null,
                new CommunityCommentPageQuery(PageRequest.of(0, 20, Sort.by("createdAt", "id")))))
        .extracting(CommunityCommentResult::commentId)
        .containsExactly(first, second);
    mvc.perform(delete(PATH + "/" + postId + "/comments/" + first).with(user(principal(owner))))
        .andExpect(status().isOk());
    assertThat(count("community_comments")).isEqualTo(2);
    assertThat(comments.findById(first)).isEmpty();
    assertThat(queries.getPost(postId, null).commentCount()).isEqualTo(1);
    mvc.perform(delete(PATH + "/" + postId + "/comments/" + first).with(user(principal(owner))))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("좋아요 반복 등록·취소는 멱등이며 본인 응답에 좋아요 상태가 반영된다")
  void likesAreIdempotent() throws Exception {
    for (int i = 0; i < 2; i++)
      mvc.perform(post(PATH + "/" + postId + "/likes").with(user(principal(owner))))
          .andExpect(status().isOk());
    assertThat(count("community_likes")).isEqualTo(1);
    mvc.perform(get(PATH).with(user(principal(owner))))
        .andExpect(jsonPath("$.data.content[0].liked").value(true))
        .andExpect(jsonPath("$.data.content[0].isOwner").value(true));
    for (int i = 0; i < 2; i++)
      mvc.perform(delete(PATH + "/" + postId + "/likes").with(user(principal(owner))))
          .andExpect(status().isOk());
    assertThat(count("community_likes")).isZero();
    assertThat(queries.getPost(postId, owner.getId()).likeCount()).isZero();
  }

  @Test
  @DisplayName("삭제된 글의 상세·수정·댓글·좋아요 접근은 404이며 댓글 기록은 남는다")
  void deletedPostsBlockAllChildActions() throws Exception {
    Long commentId =
        commands
            .createComment(owner.getId(), postId, new CreateCommunityCommentCommand("보존댓글"))
            .commentId();
    mvc.perform(delete(PATH + "/" + postId).with(user(principal(owner))))
        .andExpect(status().isOk());
    assertThat(posts.findById(postId)).isEmpty();
    assertThat(count("community_posts")).isEqualTo(1);
    assertThat(count("community_comments")).isEqualTo(1);
    mvc.perform(get(PATH + "/" + postId)).andExpect(status().isNotFound());
    mvc.perform(get(PATH + "/" + postId + "/comments")).andExpect(status().isNotFound());
    mvc.perform(
            patch(PATH + "/" + postId)
                .with(user(principal(owner)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"수정\",\"content\":\"내용\"}"))
        .andExpect(status().isNotFound());
    mvc.perform(
            post(PATH + "/" + postId + "/comments")
                .with(user(principal(owner)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"댓글\"}"))
        .andExpect(status().isNotFound());
    mvc.perform(delete(PATH + "/" + postId + "/comments/" + commentId).with(user(principal(owner))))
        .andExpect(status().isNotFound());
    mvc.perform(post(PATH + "/" + postId + "/likes").with(user(principal(owner))))
        .andExpect(status().isNotFound());
    mvc.perform(delete(PATH + "/" + postId + "/likes").with(user(principal(owner))))
        .andExpect(status().isNotFound());
    assertThat(queries.search(search(null, List.of(), List.of(), 20), null)).isEmpty();
  }

  @Test
  @DisplayName("복합 태그 검색은 중복 없는 페이징과 상위 업종 하위 포함을 보장한다")
  void filtersAndPaginationDoNotDuplicatePosts() {
    Industry parent = industries.findById(industryId).orElseThrow();
    Industry medium =
        industries.saveAndFlush(IndustryFixture.mediumChild("MD" + key, "카페중분류", parent));
    Industry small =
        industries.saveAndFlush(
            Industry.builder()
                .industryCode("SM" + key)
                .name("카페소분류")
                .level(IndustryLevel.SMALL)
                .status(IndustryStatus.ACTIVE)
                .parentIndustry(medium)
                .build());
    commands.update(
        owner.getId(),
        postId,
        save(
            "본문키워드",
            "연습",
            List.of(regionId, secondRegionId),
            List.of(medium.getId(), small.getId())));
    Long second =
        commands
            .create(owner.getId(), save("추가 글", "본문키워드", List.of(regionId), List.of(small.getId())))
            .postId();
    Page<CommunityPostResult> page =
        queries.search(
            search("본문키워드", List.of(regionId, secondRegionId), List.of(industryId), 1), null);
    assertThat(page.getTotalElements()).isEqualTo(2);
    assertThat(page.getContent()).extracting(CommunityPostResult::postId).containsExactly(second);
    Page<CommunityPostResult> next =
        queries.search(
            new CommunityPostSearchQuery(
                "본문키워드",
                List.of(regionId, secondRegionId),
                List.of(industryId),
                PageRequest.of(1, 1)),
            null);
    assertThat(next.getContent()).extracting(CommunityPostResult::postId).containsExactly(postId);
    assertThat(queries.search(search("강남구", List.of(), List.of(), 20), null).getTotalElements())
        .isEqualTo(2);
    assertThat(queries.search(search("카페소분류", List.of(), List.of(), 20), null).getTotalElements())
        .isEqualTo(2);
    assertThat(queries.search(search("추가", List.of(secondRegionId), List.of(), 20), null))
        .isEmpty();
    assertThat(
            queries
                .search(search(null, List.of(), List.of(medium.getId()), 20), null)
                .getTotalElements())
        .isEqualTo(2);
    // Root-level tags must remain visible even when their parent is null.
    commands.update(owner.getId(), postId, save("대분류 글", "내용", List.of(), List.of(industryId)));
    assertThat(
            queries
                .search(search(null, List.of(), List.of(industryId), 20), null)
                .getTotalElements())
        .isEqualTo(2);
  }

  @Test
  @DisplayName("검색 키워드의 SQL 와일드카드는 일반 문자로 취급한다")
  void escapesSearchWildcards() {
    commands.update(owner.getId(), postId, save("100% 달성", "under_score", List.of(), List.of()));
    commands.create(owner.getId(), save("일반 글", "underXscore", List.of(), List.of()));
    assertThat(queries.search(search("%", List.of(), List.of(), 20), null).getTotalElements())
        .isEqualTo(1);
    assertThat(
            queries
                .search(search("under_score", List.of(), List.of(), 20), null)
                .getTotalElements())
        .isEqualTo(1);
  }

  @Test
  @DisplayName("좋아요 정렬과 동일 시각 ID 역순 및 200자 요약을 제공한다")
  void sortsAndTruncatesSummaries() throws Exception {
    commands.update(owner.getId(), postId, save("긴 글", "가".repeat(201), List.of(), List.of()));
    Long second = commands.create(owner.getId(), save("둘째", "본문", List.of(), List.of())).postId();
    jdbc.update("update community_posts set created_at = '2026-01-01T00:00:00Z'");
    mvc.perform(get(PATH)).andExpect(jsonPath("$.data.content[0].postId").value(second));
    commands.like(owner.getId(), postId);
    mvc.perform(get(PATH).param("sort", "likeCount,desc"))
        .andExpect(jsonPath("$.data.content[0].postId").value(postId))
        .andExpect(jsonPath("$.data.content[0].contentSummary").value("가".repeat(200)));
  }

  @Test
  @DisplayName("작성자 name 미설정 시 사용자로 표시한다")
  void fallsBackForMissingAuthorName() {
    jdbc.update("update users set name = null where user_id = ?", owner.getId());
    assertThat(queries.getPost(postId, null).author().userName()).isEqualTo("사용자");
  }

  @Test
  @DisplayName("태그·작성자·좋아요 조회 쿼리 수는 페이지 게시글 수에 비례해 증가하지 않는다")
  void batchesPageAssociations() {
    for (int i = 0; i < 12; i++)
      commands.create(owner.getId(), save("글" + i, "내용", List.of(regionId), List.of(industryId)));
    var statistics =
        entityManagerFactory.unwrap(org.hibernate.SessionFactory.class).getStatistics();
    statistics.clear();
    queries.search(search(null, List.of(), List.of(), 1), owner.getId());
    long one = statistics.getPrepareStatementCount();
    statistics.clear();
    queries.search(search(null, List.of(), List.of(), 20), owner.getId());
    assertThat(statistics.getPrepareStatementCount()).isEqualTo(one);
  }

  @Test
  @DisplayName("동시 좋아요·댓글·취소 요청의 저장 건수와 카운트가 일치한다")
  void concurrentMutationsKeepCountsConsistent() throws Exception {
    race(12, i -> commands.like(owner.getId(), postId));
    assertThat(count("community_likes")).isEqualTo(1);
    assertThat(queries.getPost(postId, null).likeCount()).isEqualTo(1);
    race(
        12,
        i ->
            commands.createComment(
                owner.getId(), postId, new CreateCommunityCommentCommand("댓글" + i)));
    assertThat(count("community_comments")).isEqualTo(12);
    assertThat(queries.getPost(postId, null).commentCount()).isEqualTo(12);
    List<Long> commentIds =
        jdbc.queryForList(
            "select community_comment_id from community_comments order by community_comment_id",
            Long.class);
    race(12, i -> commands.deleteComment(owner.getId(), postId, commentIds.get(i)));
    race(12, i -> commands.unlike(owner.getId(), postId));
    assertThat(queries.getPost(postId, null).commentCount()).isZero();
    assertThat(queries.getPost(postId, null).likeCount()).isZero();
    assertThat(count("community_likes")).isZero();
    assertThat(count("community_comments")).isEqualTo(12);
  }

  @Test
  @DisplayName("게시글 삭제 잠금 이후 대기 중인 댓글·좋아요 요청은 404로 끝난다")
  void deleteWinsAgainstWaitingWrites() throws Exception {
    CountDownLatch locked = new CountDownLatch(1);
    CountDownLatch release = new CountDownLatch(1);
    try (ExecutorService executor = Executors.newFixedThreadPool(3)) {
      Future<?> deleting =
          executor.submit(
              () ->
                  new TransactionTemplate(transactionManager)
                      .executeWithoutResult(
                          status -> {
                            posts.findLiveForUpdate(postId).orElseThrow();
                            locked.countDown();
                            await(release);
                            commands.delete(owner.getId(), postId);
                          }));
      assertThat(locked.await(10, TimeUnit.SECONDS)).isTrue();
      CountDownLatch attempting = new CountDownLatch(2);
      Future<?> commenting =
          executor.submit(
              () -> {
                attempting.countDown();
                expectDeleted(
                    () ->
                        commands.createComment(
                            owner.getId(), postId, new CreateCommunityCommentCommand("댓글")));
              });
      Future<?> liking =
          executor.submit(
              () -> {
                attempting.countDown();
                expectDeleted(() -> commands.like(owner.getId(), postId));
              });
      assertThat(attempting.await(10, TimeUnit.SECONDS)).isTrue();
      release.countDown();
      deleting.get(20, TimeUnit.SECONDS);
      commenting.get(20, TimeUnit.SECONDS);
      liking.get(20, TimeUnit.SECONDS);
    } finally {
      release.countDown();
    }
    assertThat(count("community_comments")).isZero();
    assertThat(count("community_likes")).isZero();
    assertThat(
            jdbc.queryForObject(
                "select comment_count+like_count from community_posts where community_post_id=?",
                Long.class,
                postId))
        .isZero();
  }

  @Test
  @DisplayName("먼저 커밋된 댓글·좋아요는 글 삭제 후에도 기록과 카운트가 보존된다")
  void committedWritesSurvivePostDeletion() {
    commands.createComment(owner.getId(), postId, new CreateCommunityCommentCommand("보존"));
    commands.like(owner.getId(), postId);
    commands.delete(owner.getId(), postId);
    assertThat(count("community_comments")).isEqualTo(1);
    assertThat(count("community_likes")).isEqualTo(1);
    assertThat(
            jdbc.queryForObject(
                "select comment_count+like_count from community_posts where community_post_id=?",
                Long.class,
                postId))
        .isEqualTo(2);
  }

  @Test
  @DisplayName("Swagger에 커뮤니티 API·인증·오류·응답 스키마가 노출된다")
  void documentsApiContract() throws Exception {
    mvc.perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paths['/api/v1/community/posts'].post.responses['201']").exists())
        .andExpect(
            jsonPath("$.paths['/api/v1/community/posts/{postId}'].patch.responses['403']").exists())
        .andExpect(
            jsonPath(
                    "$.paths['/api/v1/community/posts/{postId}/likes'].post.security[0].bearerAuth")
                .exists())
        .andExpect(jsonPath("$.components.schemas.CommunityPostDetailResponse").exists());
  }

  private interface ConcurrentAction {
    void run(int index);
  }

  private void race(int count, ConcurrentAction action) throws Exception {
    CountDownLatch ready = new CountDownLatch(count);
    CountDownLatch start = new CountDownLatch(1);
    try (ExecutorService executor = Executors.newFixedThreadPool(count)) {
      List<Future<?>> futures = new ArrayList<>();
      for (int i = 0; i < count; i++) {
        int index = i;
        futures.add(
            executor.submit(
                () -> {
                  ready.countDown();
                  await(start);
                  action.run(index);
                }));
      }
      assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
      start.countDown();
      for (Future<?> future : futures) future.get(30, TimeUnit.SECONDS);
    } finally {
      start.countDown();
    }
  }

  private void expectDeleted(Runnable action) {
    assertThatThrownBy(action::run)
        .isInstanceOfSatisfying(
            ApplicationException.class,
            error ->
                assertThat(error.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_COMMUNITY_POST));
  }

  private void await(CountDownLatch latch) {
    try {
      if (!latch.await(15, TimeUnit.SECONDS)) throw new AssertionError("동시성 테스트 대기 시간 초과");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AssertionError(e);
    }
  }
}
