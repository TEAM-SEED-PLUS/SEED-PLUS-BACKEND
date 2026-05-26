package seed.seedplusbackend.builderstore.application.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStore;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("BuilderStoreScoreEventHandler")
class BuilderStoreScoreEventHandlerTest {

  @Mock private BuilderStoreRepository builderStoreRepository;
  @Mock private ScoreSnapshotRepository scoreSnapshotRepository;
  @InjectMocks private BuilderStoreScoreEventHandler handler;

  @Test
  @DisplayName("생성 이벤트를 처리하면 초기 PROPERTY 점수 스냅샷을 0점으로 생성한다")
  void handle_createsInitialPropertyScoreSnapshot() {
    BuilderStore builderStore = builderStore(100L);
    given(
            scoreSnapshotRepository
                .existsByBuilderStoreIdAndTargetTypeAndScoreTypeAndReferenceMonth(
                    eq(100L), eq(ScoreTargetType.BUILDER_STORE), eq(ScoreType.PROPERTY), any()))
        .willReturn(false);
    given(builderStoreRepository.findById(100L)).willReturn(Optional.of(builderStore));

    handler.handle(new BuilderStoreCreatedEvent(100L));

    ArgumentCaptor<ScoreSnapshot> captor = ArgumentCaptor.forClass(ScoreSnapshot.class);
    verify(scoreSnapshotRepository).save(captor.capture());
    ScoreSnapshot snapshot = captor.getValue();

    assertThat(snapshot.getTargetType()).isEqualTo(ScoreTargetType.BUILDER_STORE);
    assertThat(snapshot.getBuilderStore()).isEqualTo(builderStore);
    assertThat(snapshot.getScoreType()).isEqualTo(ScoreType.PROPERTY);
    assertThat(snapshot.getReferenceMonth()).isEqualTo(LocalDate.now().withDayOfMonth(1));
    assertThat(snapshot.getTotalScore()).isEqualByComparingTo(new BigDecimal("0.00"));
    assertThat(snapshot.getGrade()).isEqualTo(ScoreGrade.E);
  }

  @Test
  @DisplayName("같은 월 PROPERTY 점수 스냅샷이 이미 있으면 중복 생성하지 않는다")
  void handle_skipsWhenCurrentMonthSnapshotAlreadyExists() {
    given(
            scoreSnapshotRepository
                .existsByBuilderStoreIdAndTargetTypeAndScoreTypeAndReferenceMonth(
                    eq(100L), eq(ScoreTargetType.BUILDER_STORE), eq(ScoreType.PROPERTY), any()))
        .willReturn(true);

    handler.handle(new BuilderStoreCreatedEvent(100L));

    verify(builderStoreRepository, never()).findById(any());
    verify(scoreSnapshotRepository, never()).save(any());
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
}
