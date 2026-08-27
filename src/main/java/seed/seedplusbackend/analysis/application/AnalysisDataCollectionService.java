package seed.seedplusbackend.analysis.application;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import seed.seedplusbackend.analysis.application.result.AnalysisDataCollectionResult;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionRun;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionTask;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionTaskStatus;
import seed.seedplusbackend.analysis.domain.repository.AnalysisCollectionRunRepository;
import seed.seedplusbackend.analysis.domain.repository.AnalysisCollectionTaskRepository;
import seed.seedplusbackend.commercial.application.CommercialDataCollectService;
import seed.seedplusbackend.commercial.application.command.CommercialDataCollectCommand;
import seed.seedplusbackend.commercial.application.result.CommercialDataCollectResult;
import seed.seedplusbackend.commercial.domain.entity.CommercialDataCollectStatus;

@Service
@RequiredArgsConstructor
public class AnalysisDataCollectionService {

  private final AnalysisCollectionRunRepository runRepository;
  private final AnalysisCollectionTaskRepository taskRepository;
  private final CommercialDataCollectService commercialDataCollectService;

  public AnalysisDataCollectionResult collect(
      Long runId, List<? extends CommercialDataCollectCommand> commands) {
    if (commands == null || commands.isEmpty()) {
      throw new IllegalArgumentException("실행할 공공데이터 수집 작업이 없습니다.");
    }
    AnalysisCollectionRun run =
        runRepository
            .findById(runId)
            .orElseThrow(() -> new IllegalArgumentException("분석 데이터 수집 요청을 찾을 수 없습니다."));
    run.start();
    runRepository.save(run);

    List<String> failedDataTypes = new ArrayList<>();
    for (CommercialDataCollectCommand command : commands) {
      AnalysisCollectionTask task = findOrCreateTask(runId, run, command);
      if (task.getStatus() == AnalysisCollectionTaskStatus.COMPLETED) {
        continue;
      }
      execute(task, command, failedDataTypes);
    }

    if (failedDataTypes.isEmpty()) {
      run.complete();
    } else {
      run.fail();
    }
    runRepository.save(run);
    return new AnalysisDataCollectionResult(runId, run.getStatus(), List.copyOf(failedDataTypes));
  }

  private AnalysisCollectionTask findOrCreateTask(
      Long runId, AnalysisCollectionRun run, CommercialDataCollectCommand command) {
    return taskRepository
        .findByRunIdAndDataTypeAndTargetKey(
            runId, command.dataType().historyKey(), command.targetKey())
        .orElseGet(
            () ->
                taskRepository.save(
                    AnalysisCollectionTask.create(
                        run, command.dataType().historyKey(), command.targetKey())));
  }

  private void execute(
      AnalysisCollectionTask task,
      CommercialDataCollectCommand command,
      List<String> failedDataTypes) {
    task.start();
    taskRepository.save(task);
    try {
      CommercialDataCollectResult result = commercialDataCollectService.collect(command);
      if (result.status() == CommercialDataCollectStatus.COMPLETED) {
        task.complete();
      } else {
        fail(task, command, failedDataTypes, result.message());
      }
    } catch (RuntimeException exception) {
      fail(task, command, failedDataTypes, resolveMessage(exception));
    }
    taskRepository.save(task);
  }

  private void fail(
      AnalysisCollectionTask task,
      CommercialDataCollectCommand command,
      List<String> failedDataTypes,
      String errorMessage) {
    task.fail(errorMessage);
    failedDataTypes.add(command.dataType().historyKey());
  }

  private String resolveMessage(RuntimeException exception) {
    return exception.getMessage() == null || exception.getMessage().isBlank()
        ? "공공데이터 수집 중 알 수 없는 오류가 발생했습니다."
        : exception.getMessage();
  }
}
