package seed.seedplusbackend.analysis.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import seed.seedplusbackend.analysis.application.command.AnalysisCollectionTarget;
import seed.seedplusbackend.analysis.application.command.SmallBusinessCollectionTarget;
import seed.seedplusbackend.analysis.application.result.AnalysisDataCollectionResult;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionRun;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionRunStatus;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionType;
import seed.seedplusbackend.analysis.domain.repository.AnalysisCollectionRunRepository;
import seed.seedplusbackend.commercial.application.command.CommercialDataCollectCommand;
import seed.seedplusbackend.commercial.application.command.CommercialEstimatedSalesCollectCommand;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.support.fixture.UserFixture;
import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("분석 데이터 수집 조정기")
class AnalysisDataCollectionCoordinatorTest {

  private static final long USER_ID = 1L;
  private static final long RUN_ID = 2L;
  private static final String REGION_CODE = "1168010100";
  private static final String INDUSTRY_CODE = "G22199";

  @Mock private UserRepository userRepository;
  @Mock private AnalysisCollectionRunRepository runRepository;
  @Mock private AnalysisCollectionTargetResolver targetResolver;
  @Mock private AnalysisCollectionCommandFactory commandFactory;
  @Mock private AnalysisDataCollectionService collectionService;
  @Captor private ArgumentCaptor<AnalysisCollectionRun> runCaptor;

  @Test
  @DisplayName("사용자와 분석 조건으로 수집 실행을 생성하고 모든 명령을 실행한다")
  void createsRunAndCollectsCommands() {
    User user = UserFixture.generalActiveUser("collection-coordinator@test.com");
    AnalysisCollectionTarget target =
        new AnalysisCollectionTarget(
            "20262", List.of(new SmallBusinessCollectionTarget("10117", "G2", "G221", "G22199")));
    List<CommercialDataCollectCommand> commands =
        List.of(new CommercialEstimatedSalesCollectCommand("20262", true));
    AnalysisDataCollectionResult expected =
        new AnalysisDataCollectionResult(RUN_ID, AnalysisCollectionRunStatus.COMPLETED, List.of());
    given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
    given(targetResolver.resolve(REGION_CODE, INDUSTRY_CODE)).willReturn(target);
    given(commandFactory.create(target)).willReturn(commands);
    given(runRepository.save(any(AnalysisCollectionRun.class)))
        .willAnswer(
            invocation -> {
              AnalysisCollectionRun run = invocation.getArgument(0);
              ReflectionTestUtils.setField(run, "id", RUN_ID);
              return run;
            });
    given(collectionService.collect(RUN_ID, commands)).willReturn(expected);

    AnalysisDataCollectionResult result =
        coordinator().collect(USER_ID, AnalysisCollectionType.PROFIT, REGION_CODE, INDUSTRY_CODE);

    assertThat(result).isSameAs(expected);
    verify(runRepository).save(runCaptor.capture());
    assertThat(runCaptor.getValue())
        .extracting(
            AnalysisCollectionRun::getUser,
            AnalysisCollectionRun::getAnalysisType,
            AnalysisCollectionRun::getRegionCode,
            AnalysisCollectionRun::getIndustryCode)
        .containsExactly(user, AnalysisCollectionType.PROFIT, REGION_CODE, INDUSTRY_CODE);
  }

  @Test
  @DisplayName("존재하지 않는 사용자의 수집 실행은 만들지 않는다")
  void rejectsUnknownUser() {
    given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                coordinator()
                    .collect(USER_ID, AnalysisCollectionType.PROFIT, REGION_CODE, INDUSTRY_CODE))
        .isInstanceOf(ApplicationException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.NOT_FOUND_USER);
    verify(targetResolver, never()).resolve(REGION_CODE, INDUSTRY_CODE);
    verify(runRepository, never()).save(any());
  }

  @Test
  @DisplayName("기존 실행을 재시도하면 같은 실행 ID로 수집 명령을 다시 전달한다")
  void retriesExistingRun() {
    User user = UserFixture.generalActiveUser("collection-retry@test.com");
    AnalysisCollectionRun run =
        AnalysisCollectionRun.create(
            user, AnalysisCollectionType.SURVIVAL, REGION_CODE, INDUSTRY_CODE);
    AnalysisCollectionTarget target =
        new AnalysisCollectionTarget(
            "20262", List.of(new SmallBusinessCollectionTarget("10117", "G2", "G221", "G22199")));
    List<CommercialDataCollectCommand> commands =
        List.of(new CommercialEstimatedSalesCollectCommand("20262", true));
    AnalysisDataCollectionResult expected =
        new AnalysisDataCollectionResult(RUN_ID, AnalysisCollectionRunStatus.COMPLETED, List.of());
    given(runRepository.findByIdAndUserId(RUN_ID, USER_ID)).willReturn(Optional.of(run));
    given(targetResolver.resolve(REGION_CODE, INDUSTRY_CODE)).willReturn(target);
    given(commandFactory.create(target)).willReturn(commands);
    given(collectionService.collect(RUN_ID, commands)).willReturn(expected);

    AnalysisDataCollectionResult result = coordinator().retry(USER_ID, RUN_ID);

    assertThat(result).isSameAs(expected);
    verify(collectionService).collect(RUN_ID, commands);
    verify(runRepository, never()).save(any());
  }

  @Test
  @DisplayName("사용자에게 속하지 않은 수집 실행은 재시도하지 않는다")
  void rejectsRetryForUnownedRun() {
    given(runRepository.findByIdAndUserId(RUN_ID, USER_ID)).willReturn(Optional.empty());

    assertThatThrownBy(() -> coordinator().retry(USER_ID, RUN_ID))
        .isInstanceOf(ApplicationException.class)
        .hasMessageContaining(String.valueOf(RUN_ID))
        .extracting("errorCode")
        .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    verify(targetResolver, never()).resolve(any(), any());
    verify(collectionService, never()).collect(any(), any());
  }

  private AnalysisDataCollectionCoordinator coordinator() {
    return new AnalysisDataCollectionCoordinator(
        userRepository, runRepository, targetResolver, commandFactory, collectionService);
  }
}
