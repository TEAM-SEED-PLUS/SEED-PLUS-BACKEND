package seed.seedplusbackend.analysis.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seed.seedplusbackend.analysis.application.result.AnalysisDataCollectionResult;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionRun;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionRunStatus;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionTask;
import seed.seedplusbackend.analysis.domain.repository.AnalysisCollectionRunRepository;
import seed.seedplusbackend.analysis.domain.repository.AnalysisCollectionTaskRepository;
import seed.seedplusbackend.commercial.application.CommercialDataCollectService;
import seed.seedplusbackend.commercial.application.command.CommercialDataCollectCommand;
import seed.seedplusbackend.commercial.application.command.CommercialEstimatedSalesCollectCommand;
import seed.seedplusbackend.commercial.application.command.KosisBusinessSurvivalCollectCommand;
import seed.seedplusbackend.commercial.application.result.CommercialDataCollectResult;
import seed.seedplusbackend.commercial.domain.entity.CommercialDataCollectStatus;
import seed.seedplusbackend.support.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
@DisplayName("분석용 공공데이터 수집 서비스")
class AnalysisDataCollectionServiceTest {

  private static final long RUN_ID = 1L;

  @Mock private AnalysisCollectionRunRepository runRepository;
  @Mock private AnalysisCollectionTaskRepository taskRepository;
  @Mock private CommercialDataCollectService commercialDataCollectService;

  private AnalysisDataCollectionService service;
  private AnalysisCollectionRun run;
  private Map<String, AnalysisCollectionTask> tasks;

  @BeforeEach
  void setUp() {
    service =
        new AnalysisDataCollectionService(
            runRepository, taskRepository, commercialDataCollectService);
    run =
        AnalysisCollectionRun.create(
            UserFixture.generalActiveUser("collection-service@test.com"),
            seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionType.SURVIVAL,
            "1168010100",
            "I101");
    tasks = new HashMap<>();
    given(runRepository.findById(RUN_ID)).willReturn(Optional.of(run));
    given(runRepository.save(run)).willReturn(run);
    org.mockito.Mockito.lenient()
        .when(taskRepository.save(org.mockito.ArgumentMatchers.any()))
        .thenAnswer(invocation -> invocation.getArgument(0));
    org.mockito.Mockito.lenient()
        .when(
            taskRepository.findByRunIdAndDataTypeAndTargetKey(
                org.mockito.ArgumentMatchers.eq(RUN_ID),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()))
        .thenAnswer(
            invocation ->
                Optional.ofNullable(
                    tasks.get(invocation.getArgument(1) + ":" + invocation.getArgument(2))));
  }

  @Test
  @DisplayName("한 작업이 실패해도 다음 작업을 계속 실행한다")
  void continuesAfterOneTaskFails() {
    CommercialDataCollectCommand sales = new CommercialEstimatedSalesCollectCommand("20262", false);
    CommercialDataCollectCommand survival =
        new KosisBusinessSurvivalCollectCommand(null, null, 3, false);
    given(commercialDataCollectService.collect(sales))
        .willThrow(new IllegalStateException("sales failed"));
    given(commercialDataCollectService.collect(survival)).willReturn(completed(survival));

    AnalysisDataCollectionResult result = service.collect(RUN_ID, List.of(sales, survival));

    verify(commercialDataCollectService).collect(survival);
    assertThat(result.status()).isEqualTo(AnalysisCollectionRunStatus.FAILED);
    assertThat(result.failedDataTypes()).containsExactly(sales.dataType().historyKey());
  }

  @Test
  @DisplayName("재실행 시 완료 작업은 건너뛰고 실패 작업만 다시 실행한다")
  void retriesOnlyFailedTask() {
    CommercialDataCollectCommand sales = new CommercialEstimatedSalesCollectCommand("20262", false);
    CommercialDataCollectCommand survival =
        new KosisBusinessSurvivalCollectCommand(null, null, 3, false);
    AnalysisCollectionTask completedTask = task(sales);
    completedTask.start();
    completedTask.complete();
    AnalysisCollectionTask failedTask = task(survival);
    failedTask.start();
    failedTask.fail("timeout");
    remember(completedTask);
    remember(failedTask);
    given(commercialDataCollectService.collect(survival)).willReturn(completed(survival));

    AnalysisDataCollectionResult result = service.collect(RUN_ID, List.of(sales, survival));

    verify(commercialDataCollectService, never()).collect(sales);
    verify(commercialDataCollectService).collect(survival);
    assertThat(failedTask.getAttemptCount()).isEqualTo(2);
    assertThat(result.status()).isEqualTo(AnalysisCollectionRunStatus.COMPLETED);
  }

  private AnalysisCollectionTask task(CommercialDataCollectCommand command) {
    return AnalysisCollectionTask.create(run, command.dataType().historyKey(), command.targetKey());
  }

  private void remember(AnalysisCollectionTask task) {
    tasks.put(task.getDataType() + ":" + task.getTargetKey(), task);
  }

  private CommercialDataCollectResult completed(CommercialDataCollectCommand command) {
    return new CommercialDataCollectResult(
        command.dataType().historyKey(),
        command.targetKey(),
        1,
        1,
        false,
        CommercialDataCollectStatus.COMPLETED,
        "완료");
  }
}
