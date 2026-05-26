package seed.seedplusbackend.builderstore.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import seed.seedplusbackend.builderstore.application.result.BuilderStoreDetailResult;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStore;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreVisibilityStatus;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreBookmarkRepository;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreCommentRepository;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreImageRepository;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreLikeRepository;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreRepository;
import seed.seedplusbackend.score.domain.entity.ScoreGrade;
import seed.seedplusbackend.score.domain.entity.ScoreSnapshot;
import seed.seedplusbackend.score.domain.entity.ScoreTargetType;
import seed.seedplusbackend.score.domain.entity.ScoreType;
import seed.seedplusbackend.score.domain.repository.ScoreSnapshotRepository;
import seed.seedplusbackend.support.fixture.BuilderStoreFixture;
import seed.seedplusbackend.support.fixture.CommercialAreaFixture;
import seed.seedplusbackend.support.fixture.IndustryFixture;
import seed.seedplusbackend.support.fixture.RegionFixture;
import seed.seedplusbackend.support.fixture.UserFixture;
import seed.seedplusbackend.user.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("BuilderStoreQueryService")
class BuilderStoreQueryServiceTest {

  @Mock private BuilderStoreRepository builderStoreRepository;
  @Mock private BuilderStoreImageRepository builderStoreImageRepository;
  @Mock private BuilderStoreLikeRepository builderStoreLikeRepository;
  @Mock private BuilderStoreBookmarkRepository builderStoreBookmarkRepository;
  @Mock private BuilderStoreCommentRepository builderStoreCommentRepository;
  @Mock private ScoreSnapshotRepository scoreSnapshotRepository;
  @Mock private UserRepository userRepository;
  @InjectMocks private BuilderStoreQueryService builderStoreQueryService;

  @Test
  @DisplayName("상세 조회는 최신 PROPERTY 점수 스냅샷만 사용한다")
  void getPublicBuilderStore_usesLatestPropertyScoreSnapshot() {
    BuilderStore builderStore = builderStore(100L);
    ScoreSnapshot propertyScore = propertyScore(builderStore);
    given(
            builderStoreRepository.findByIdAndVisibilityStatus(
                100L, BuilderStoreVisibilityStatus.PUBLIC))
        .willReturn(Optional.of(builderStore));
    given(builderStoreImageRepository.findByBuilderStore_IdOrderByDisplayOrderAscIdAsc(100L))
        .willReturn(List.of());
    given(
            scoreSnapshotRepository
                .findFirstByBuilderStoreIdAndTargetTypeAndScoreTypeOrderByReferenceMonthDescIdDesc(
                    100L, ScoreTargetType.BUILDER_STORE, ScoreType.PROPERTY))
        .willReturn(Optional.of(propertyScore));

    BuilderStoreDetailResult result = builderStoreQueryService.getPublicBuilderStore(100L, null);

    assertThat(result.latestScore()).isEqualTo(propertyScore);
    verify(scoreSnapshotRepository)
        .findFirstByBuilderStoreIdAndTargetTypeAndScoreTypeOrderByReferenceMonthDescIdDesc(
            100L, ScoreTargetType.BUILDER_STORE, ScoreType.PROPERTY);
  }

  private BuilderStore builderStore(Long id) {
    BuilderStore builderStore =
        BuilderStoreFixture.publicBuilderStore(
            UserFixture.generalActiveUser("01012345678"),
            RegionFixture.seoulGangnamYeoksamLegalDong(),
            CommercialAreaFixture.developedActive("상권"),
            IndustryFixture.largeRoot("IND", "음식점업"));
    ReflectionTestUtils.setField(builderStore, "id", id);
    return builderStore;
  }

  private ScoreSnapshot propertyScore(BuilderStore builderStore) {
    return ScoreSnapshot.builder()
        .targetType(ScoreTargetType.BUILDER_STORE)
        .store(null)
        .builderStore(builderStore)
        .commercialArea(null)
        .region(null)
        .scoreType(ScoreType.PROPERTY)
        .referenceMonth(LocalDate.of(2026, 5, 1))
        .totalScore(new BigDecimal("75.00"))
        .grade(ScoreGrade.B)
        .pd(null)
        .survivalProbability(null)
        .build();
  }
}
