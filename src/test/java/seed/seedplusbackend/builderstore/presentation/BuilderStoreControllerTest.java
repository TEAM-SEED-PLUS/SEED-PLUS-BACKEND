package seed.seedplusbackend.builderstore.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import seed.seedplusbackend.builderstore.application.BuilderStoreCommandService;
import seed.seedplusbackend.builderstore.application.BuilderStoreQueryService;
import seed.seedplusbackend.builderstore.application.command.CreateBuilderStoreCommand;
import seed.seedplusbackend.builderstore.application.command.UpdateBuilderStoreCommand;
import seed.seedplusbackend.builderstore.application.result.BuilderStoreCommentResult;
import seed.seedplusbackend.builderstore.application.result.BuilderStoreDetailResult;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStore;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreComment;
import seed.seedplusbackend.global.error.GlobalExceptionHandler;
import seed.seedplusbackend.global.security.AuthenticatedUser;
import seed.seedplusbackend.support.fixture.BuilderStoreFixture;
import seed.seedplusbackend.support.fixture.CommercialAreaFixture;
import seed.seedplusbackend.support.fixture.IndustryFixture;
import seed.seedplusbackend.support.fixture.RegionFixture;
import seed.seedplusbackend.support.fixture.UserFixture;
import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.domain.entity.UserRole;

@ExtendWith(MockitoExtension.class)
@DisplayName("BuilderStoreController")
class BuilderStoreControllerTest {

  private MockMvc mockMvc;

  @Mock private BuilderStoreQueryService builderStoreQueryService;
  @Mock private BuilderStoreCommandService builderStoreCommandService;

  @BeforeEach
  void setUp() {
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();
    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new BuilderStoreController(builderStoreQueryService, builderStoreCommandService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .setValidator(validator)
            .build();
  }

  @Test
  @DisplayName("공개 목록 조회는 페이지 응답 구조를 반환한다")
  void getBuilderStores_returnsPageResponse() throws Exception {
    BuilderStore builderStore = builderStore(1L, user(1L));
    given(builderStoreQueryService.searchPublic(any()))
        .willReturn(new PageImpl<>(List.of(builderStore), PageRequest.of(0, 20), 1));

    mockMvc
        .perform(get("/api/v1/builder-stores"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.data.content[0].builderStoreId").value(1))
        .andExpect(jsonPath("$.data.pageInfo.page").value(0))
        .andExpect(jsonPath("$.data.pageInfo.size").value(20))
        .andExpect(jsonPath("$.data.pageInfo.totalElements").value(1));
  }

  @Test
  @DisplayName("생성 요청의 필수값이 누락되면 400 Bad Request를 반환한다")
  void createBuilderStore_returnsBadRequest_whenRequiredFieldMissing() throws Exception {
    String request =
        """
        {
          "commercialAreaId": 1,
          "industryId": 1,
          "name": "강남 샐러드 창업안",
          "metrics": {
            "area": 40,
            "expectedMonthlySales": 50000000,
            "expectedProfitRate": 12.50,
            "investmentPaybackMonths": 36,
            "monthlyRent": 2000000,
            "deposit": 20000000,
            "investmentAmount": 100000000
          }
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/builder-stores").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("생성 요청은 metrics 구조와 인증 사용자 ID를 서비스로 전달한다")
  void createBuilderStore_passesMetricsAndAuthenticatedUserIdToService() throws Exception {
    User owner = user(9L);
    BuilderStore builderStore = builderStore(100L, owner);
    given(builderStoreCommandService.create(eq(9L), any(CreateBuilderStoreCommand.class)))
        .willReturn(new BuilderStoreDetailResult(builderStore, List.of(), null, false, false));

    String request =
        """
        {
          "regionId": 1,
          "commercialAreaId": 1,
          "industryId": 1,
          "name": "강남 샐러드 창업안",
          "metrics": {
            "area": 40,
            "expectedMonthlySales": 50000000,
            "expectedProfitRate": 12.50,
            "investmentPaybackMonths": 36,
            "monthlyRent": 2000000,
            "deposit": 20000000,
            "investmentAmount": 100000000
          },
          "visibilityStatus": "PUBLIC",
          "imageUrls": []
        }
        """;

    try {
      SecurityContextHolder.getContext().setAuthentication(userAuthentication());

      mockMvc
          .perform(
              post("/api/v1/builder-stores")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(request))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.status").value(201))
          .andExpect(jsonPath("$.data.builderStoreId").value(100));
    } finally {
      SecurityContextHolder.clearContext();
    }

    verify(builderStoreCommandService).create(eq(9L), any(CreateBuilderStoreCommand.class));
  }

  @Test
  @DisplayName("생성 요청의 metrics 필수값이 누락되면 400 Bad Request를 반환한다")
  void createBuilderStore_returnsBadRequest_whenMetricsRequiredFieldMissing() throws Exception {
    String request =
        """
        {
          "regionId": 1,
          "commercialAreaId": 1,
          "industryId": 1,
          "name": "강남 샐러드 창업안",
          "metrics": {
            "expectedMonthlySales": 50000000,
            "expectedProfitRate": 12.50,
            "investmentPaybackMonths": 36,
            "monthlyRent": 2000000,
            "deposit": 20000000,
            "investmentAmount": 100000000
          }
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/builder-stores").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("수정 요청은 name 없이 metrics 일부만 수정할 수 있다")
  void updateBuilderStore_allowsMissingNameAndPartialMetrics() throws Exception {
    User owner = user(9L);
    BuilderStore builderStore = builderStore(100L, owner);
    given(builderStoreCommandService.update(eq(9L), eq(100L), any(UpdateBuilderStoreCommand.class)))
        .willReturn(new BuilderStoreDetailResult(builderStore, List.of(), null, false, false));

    String request =
        """
        {
          "metrics": {
            "area": 55
          }
        }
        """;

    try {
      SecurityContextHolder.getContext().setAuthentication(userAuthentication());

      mockMvc
          .perform(
              patch("/api/v1/builder-stores/100")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(request))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value(200));
    } finally {
      SecurityContextHolder.clearContext();
    }

    verify(builderStoreCommandService)
        .update(eq(9L), eq(100L), any(UpdateBuilderStoreCommand.class));
  }

  @Test
  @DisplayName("수정 요청의 name이 공백이면 400 Bad Request를 반환한다")
  void updateBuilderStore_returnsBadRequest_whenNameIsBlank() throws Exception {
    String request =
        """
        {
          "name": "   "
        }
        """;

    try {
      SecurityContextHolder.getContext().setAuthentication(userAuthentication());

      mockMvc
          .perform(
              patch("/api/v1/builder-stores/100")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(request))
          .andExpect(status().isBadRequest());
    } finally {
      SecurityContextHolder.clearContext();
    }
  }

  @Test
  @DisplayName("댓글 목록 조회는 작성자와 대댓글 정보를 포함해 반환한다")
  void getComments_returnsAuthorAndReplies() throws Exception {
    User owner = user(1L);
    User author = user(2L);
    ReflectionTestUtils.setField(author, "name", "Author");
    BuilderStore builderStore = builderStore(100L, owner);
    BuilderStoreComment root = comment(10L, builderStore, null, author, "root comment");
    BuilderStoreComment reply = comment(11L, builderStore, root, author, "reply comment");

    given(builderStoreQueryService.getComments(eq(100L), any()))
        .willReturn(
            new PageImpl<>(
                List.of(new BuilderStoreCommentResult(root, List.of(reply))),
                PageRequest.of(0, 20),
                1));

    mockMvc
        .perform(get("/api/v1/builder-stores/100/comments"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.data.content[0].commentId").value(10))
        .andExpect(jsonPath("$.data.content[0].userId").value(2))
        .andExpect(jsonPath("$.data.content[0].userName").value("Author"))
        .andExpect(jsonPath("$.data.content[0].replies[0].commentId").value(11))
        .andExpect(jsonPath("$.data.content[0].replies[0].parentCommentId").value(10))
        .andExpect(jsonPath("$.data.content[0].replies[0].content").value("reply comment"));
  }

  private UsernamePasswordAuthenticationToken userAuthentication() {
    AuthenticatedUser authenticatedUser =
        new AuthenticatedUser(9L, "01012345678", UserRole.GENERAL);
    return new UsernamePasswordAuthenticationToken(
        authenticatedUser, null, authenticatedUser.getAuthorities());
  }

  private BuilderStore builderStore(Long id, User owner) {
    BuilderStore builderStore =
        BuilderStoreFixture.publicBuilderStore(
            owner,
            RegionFixture.seoulGangnamYeoksamLegalDong(),
            CommercialAreaFixture.developedActive("상권"),
            IndustryFixture.largeRoot("IND", "음식점업"));
    ReflectionTestUtils.setField(builderStore, "id", id);
    return builderStore;
  }

  private BuilderStoreComment comment(
      Long id, BuilderStore builderStore, BuilderStoreComment parent, User user, String content) {
    BuilderStoreComment comment =
        BuilderStoreComment.builder()
            .builderStore(builderStore)
            .parent(parent)
            .user(user)
            .content(content)
            .build();
    ReflectionTestUtils.setField(comment, "id", id);
    return comment;
  }

  private User user(Long id) {
    User user = UserFixture.generalActiveUser("01012345678");
    ReflectionTestUtils.setField(user, "id", id);
    return user;
  }
}
