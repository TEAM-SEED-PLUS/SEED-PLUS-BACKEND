package seed.seedplusbackend.builderstore.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import seed.seedplusbackend.builderstore.application.command.BuilderStoreMetricsCommand;
import seed.seedplusbackend.builderstore.application.command.CreateBuilderStoreCommand;
import seed.seedplusbackend.builderstore.application.command.CreateBuilderStoreCommentCommand;
import seed.seedplusbackend.builderstore.application.command.UpdateBuilderStoreCommand;
import seed.seedplusbackend.builderstore.application.event.BuilderStoreCreatedEvent;
import seed.seedplusbackend.builderstore.application.result.BuilderStoreDetailResult;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStore;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreComment;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreLike;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreVisibilityStatus;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreBookmarkRepository;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreCommentRepository;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreImageRepository;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreLikeRepository;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreRepository;
import seed.seedplusbackend.building.domain.repository.BuildingRepository;
import seed.seedplusbackend.commercial.domain.repository.CommercialAreaRepository;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.industry.domain.repository.IndustryRepository;
import seed.seedplusbackend.region.domain.repository.RegionRepository;
import seed.seedplusbackend.score.domain.repository.ScoreSnapshotRepository;
import seed.seedplusbackend.support.fixture.BuilderStoreFixture;
import seed.seedplusbackend.support.fixture.CommercialAreaFixture;
import seed.seedplusbackend.support.fixture.IndustryFixture;
import seed.seedplusbackend.support.fixture.RegionFixture;
import seed.seedplusbackend.support.fixture.UserFixture;
import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("BuilderStoreCommandService")
class BuilderStoreCommandServiceTest {

  @Mock private BuilderStoreRepository builderStoreRepository;
  @Mock private BuilderStoreImageRepository builderStoreImageRepository;
  @Mock private BuilderStoreLikeRepository builderStoreLikeRepository;
  @Mock private BuilderStoreBookmarkRepository builderStoreBookmarkRepository;
  @Mock private BuilderStoreCommentRepository builderStoreCommentRepository;
  @Mock private UserRepository userRepository;
  @Mock private RegionRepository regionRepository;
  @Mock private CommercialAreaRepository commercialAreaRepository;
  @Mock private IndustryRepository industryRepository;
  @Mock private BuildingRepository buildingRepository;
  @Mock private ScoreSnapshotRepository scoreSnapshotRepository;
  @Mock private ApplicationEventPublisher eventPublisher;
  @InjectMocks private BuilderStoreCommandService builderStoreCommandService;

  private User owner;

  @BeforeEach
  void setUp() {
    owner = user(1L);
  }

  @Test
  @DisplayName("생성 시 지역이 없으면 NOT_FOUND_REGION 예외가 발생한다")
  void create_throwsNotFoundRegion_whenRegionMissing() {
    given(userRepository.findById(1L)).willReturn(Optional.of(owner));
    given(regionRepository.findById(10L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> builderStoreCommandService.create(1L, createCommand()))
        .isInstanceOf(ApplicationException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.NOT_FOUND_REGION);
  }

  @Test
  @DisplayName("생성 시 propertyScore는 0으로 시작하고 생성 이벤트를 발행한다")
  void create_setsInitialPropertyScoreAndPublishesEvent() {
    given(userRepository.findById(1L)).willReturn(Optional.of(owner));
    given(regionRepository.findById(10L))
        .willReturn(Optional.of(RegionFixture.seoulGangnamYeoksamLegalDong()));
    given(commercialAreaRepository.findByIdAndStatusNot(any(), any()))
        .willReturn(Optional.of(CommercialAreaFixture.developedActive("생성 상권")));
    given(industryRepository.findByIdAndStatus(any(), any()))
        .willReturn(Optional.of(IndustryFixture.largeRoot("CREATE-IND", "음식점업")));
    given(builderStoreRepository.save(any(BuilderStore.class)))
        .willAnswer(
            invocation -> {
              BuilderStore builderStore = invocation.getArgument(0);
              ReflectionTestUtils.setField(builderStore, "id", 100L);
              return builderStore;
            });
    given(builderStoreImageRepository.findByBuilderStore_IdOrderByDisplayOrderAscIdAsc(100L))
        .willReturn(List.of());
    given(
            scoreSnapshotRepository
                .findFirstByBuilderStoreIdAndTargetTypeAndScoreTypeOrderByReferenceMonthDescIdDesc(
                    any(), any(), any()))
        .willReturn(Optional.empty());

    BuilderStoreDetailResult result = builderStoreCommandService.create(1L, createCommand());

    assertThat(result.builderStore().getPropertyScore()).isZero();
    verify(eventPublisher).publishEvent(new BuilderStoreCreatedEvent(100L));
    verify(scoreSnapshotRepository, never()).save(any());
  }

  @Test
  @DisplayName("수정 시 소유자가 아니면 NOT_OWNER_BUILDER_STORE 예외가 발생한다")
  void update_throwsNotOwner_whenUserIsNotOwner() {
    BuilderStore builderStore = builderStore(owner, 100L);
    given(
            builderStoreRepository.findByIdAndVisibilityStatusNot(
                100L, BuilderStoreVisibilityStatus.DELETED))
        .willReturn(Optional.of(builderStore));

    assertThatThrownBy(
            () ->
                builderStoreCommandService.update(
                    2L,
                    100L,
                    new UpdateBuilderStoreCommand(
                        null, null, null, null, "수정", null, null, null, null)))
        .isInstanceOf(ApplicationException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.NOT_OWNER_BUILDER_STORE);
  }

  @Test
  @DisplayName("수정 시 metrics가 없으면 지표와 propertyScore를 유지한다")
  void update_keepsMetricsAndPropertyScore_whenMetricsIsNull() {
    BuilderStore builderStore = builderStore(owner, 100L);
    given(
            builderStoreRepository.findByIdAndVisibilityStatusNot(
                100L, BuilderStoreVisibilityStatus.DELETED))
        .willReturn(Optional.of(builderStore));
    given(builderStoreImageRepository.findByBuilderStore_IdOrderByDisplayOrderAscIdAsc(100L))
        .willReturn(List.of());
    given(
            scoreSnapshotRepository
                .findFirstByBuilderStoreIdAndTargetTypeAndScoreTypeOrderByReferenceMonthDescIdDesc(
                    any(), any(), any()))
        .willReturn(Optional.empty());

    builderStoreCommandService.update(
        1L,
        100L,
        new UpdateBuilderStoreCommand(
            null, null, null, null, "수정", null, null, BuilderStoreVisibilityStatus.PRIVATE, null));

    assertThat(builderStore.getName()).isEqualTo("수정");
    assertThat(builderStore.getArea()).isEqualTo(40);
    assertThat(builderStore.getExpectedMonthlySales()).isEqualTo(50_000_000L);
    assertThat(builderStore.getPropertyScore()).isEqualTo(80);
  }

  @Test
  @DisplayName("수정 시 metrics 일부 필드만 있으면 해당 지표만 변경한다")
  void update_updatesPartialMetrics() {
    BuilderStore builderStore = builderStore(owner, 100L);
    given(
            builderStoreRepository.findByIdAndVisibilityStatusNot(
                100L, BuilderStoreVisibilityStatus.DELETED))
        .willReturn(Optional.of(builderStore));
    given(builderStoreImageRepository.findByBuilderStore_IdOrderByDisplayOrderAscIdAsc(100L))
        .willReturn(List.of());
    given(
            scoreSnapshotRepository
                .findFirstByBuilderStoreIdAndTargetTypeAndScoreTypeOrderByReferenceMonthDescIdDesc(
                    any(), any(), any()))
        .willReturn(Optional.empty());

    builderStoreCommandService.update(
        1L,
        100L,
        new UpdateBuilderStoreCommand(
            null,
            null,
            null,
            null,
            null,
            new BuilderStoreMetricsCommand(55, null, null, null, null, null, null),
            null,
            null,
            null));

    assertThat(builderStore.getArea()).isEqualTo(55);
    assertThat(builderStore.getExpectedMonthlySales()).isEqualTo(50_000_000L);
    assertThat(builderStore.getPropertyScore()).isEqualTo(80);
  }

  @Test
  @DisplayName("이미 좋아요한 빌더스토어는 다시 좋아요할 수 없다")
  void like_throwsAlreadyLiked_whenLikeExists() {
    User user = user(1L);
    BuilderStore builderStore = builderStore(user, 100L);
    given(userRepository.findById(1L)).willReturn(Optional.of(user));
    given(
            builderStoreRepository.findByIdAndVisibilityStatus(
                100L, BuilderStoreVisibilityStatus.PUBLIC))
        .willReturn(Optional.of(builderStore));
    given(builderStoreLikeRepository.existsByBuilderStore_IdAndUser_Id(100L, 1L)).willReturn(true);

    assertThatThrownBy(() -> builderStoreCommandService.like(1L, 100L))
        .isInstanceOf(ApplicationException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.ALREADY_LIKED);

    verify(builderStoreLikeRepository, never()).save(any());
  }

  @Test
  @DisplayName("대댓글에는 대댓글을 작성할 수 없다")
  void createComment_throwsInvalidParentComment_whenParentIsReply() {
    User user = user(1L);
    BuilderStore builderStore = builderStore(user, 100L);
    BuilderStoreComment parent =
        BuilderStoreComment.builder()
            .builderStore(builderStore)
            .parent(null)
            .user(user)
            .content("부모")
            .build();
    ReflectionTestUtils.setField(parent, "id", 10L);
    BuilderStoreComment reply =
        BuilderStoreComment.builder()
            .builderStore(builderStore)
            .parent(parent)
            .user(user)
            .content("대댓글")
            .build();
    ReflectionTestUtils.setField(reply, "id", 11L);

    given(userRepository.findById(1L)).willReturn(Optional.of(user));
    given(
            builderStoreRepository.findByIdAndVisibilityStatus(
                100L, BuilderStoreVisibilityStatus.PUBLIC))
        .willReturn(Optional.of(builderStore));
    given(builderStoreCommentRepository.findByIdAndBuilderStore_Id(11L, 100L))
        .willReturn(Optional.of(reply));

    assertThatThrownBy(
            () ->
                builderStoreCommandService.createComment(
                    1L, 100L, new CreateBuilderStoreCommentCommand(11L, "대댓글의 대댓글")))
        .isInstanceOf(ApplicationException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.INVALID_PARENT_COMMENT);
  }

  @Test
  @DisplayName("부모 댓글을 삭제하면 직계 대댓글과 댓글 수가 함께 감소한다")
  void deleteComment_deletesRepliesAndDecreasesCommentCount_whenParentCommentDeleted() {
    User user = user(1L);
    BuilderStore builderStore = builderStore(user, 100L);
    builderStore.increaseCommentCount();
    builderStore.increaseCommentCount();
    BuilderStoreComment parent =
        BuilderStoreComment.builder()
            .builderStore(builderStore)
            .parent(null)
            .user(user)
            .content("부모")
            .build();
    ReflectionTestUtils.setField(parent, "id", 10L);
    BuilderStoreComment reply =
        BuilderStoreComment.builder()
            .builderStore(builderStore)
            .parent(parent)
            .user(user)
            .content("대댓글")
            .build();
    ReflectionTestUtils.setField(reply, "id", 11L);

    given(
            builderStoreRepository.findByIdAndVisibilityStatusNot(
                100L, BuilderStoreVisibilityStatus.DELETED))
        .willReturn(Optional.of(builderStore));
    given(builderStoreCommentRepository.findByIdAndBuilderStore_Id(10L, 100L))
        .willReturn(Optional.of(parent));
    given(builderStoreCommentRepository.findByParent_IdOrderByCreatedAtAscIdAsc(10L))
        .willReturn(List.of(reply));

    builderStoreCommandService.deleteComment(1L, 100L, 10L);

    verify(builderStoreCommentRepository).delete(reply);
    verify(builderStoreCommentRepository).delete(parent);
    assertThat(builderStore.getCommentCount()).isZero();
  }

  @SuppressWarnings("unused")
  private BuilderStoreLike like(BuilderStore builderStore, User user) {
    return BuilderStoreLike.builder().builderStore(builderStore).user(user).build();
  }

  private CreateBuilderStoreCommand createCommand() {
    return new CreateBuilderStoreCommand(
        10L,
        20L,
        30L,
        null,
        "생성 빌더스토어",
        new BuilderStoreMetricsCommand(
            40, 50_000_000L, new BigDecimal("12.50"), 36, 2_000_000L, 20_000_000L, 100_000_000L),
        "설명",
        BuilderStoreVisibilityStatus.PUBLIC,
        List.of("https://example.com/image.png"));
  }

  private BuilderStore builderStore(User owner, Long id) {
    BuilderStore builderStore =
        BuilderStoreFixture.publicBuilderStore(
            owner,
            RegionFixture.seoulGangnamYeoksamLegalDong(),
            CommercialAreaFixture.developedActive("상권"),
            IndustryFixture.largeRoot("IND", "음식점업"));
    ReflectionTestUtils.setField(builderStore, "id", id);
    ReflectionTestUtils.setField(builderStore, "uploadedAt", OffsetDateTime.now());
    return builderStore;
  }

  private User user(Long id) {
    User user = UserFixture.generalActiveUser("01012345678");
    ReflectionTestUtils.setField(user, "id", id);
    return user;
  }
}
