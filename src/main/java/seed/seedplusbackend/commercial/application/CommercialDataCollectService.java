package seed.seedplusbackend.commercial.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import seed.seedplusbackend.commercial.application.command.CommercialDataCollectCommand;
import seed.seedplusbackend.commercial.application.provider.CommercialDataProvider;
import seed.seedplusbackend.commercial.application.provider.CommercialDataProviderRegistry;
import seed.seedplusbackend.commercial.application.provider.CommercialDataType;
import seed.seedplusbackend.commercial.application.result.CommercialDataCollectResult;
import seed.seedplusbackend.commercial.domain.entity.CommercialDataCollectHistory;
import seed.seedplusbackend.commercial.domain.entity.CommercialDataCollectStatus;
import seed.seedplusbackend.commercial.domain.repository.CommercialDataCollectHistoryRepository;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommercialDataCollectService {

  private final CommercialDataProviderRegistry providerRegistry;
  private final CommercialDataCollectHistoryRepository historyRepository;

  public CommercialDataCollectResult collect(CommercialDataCollectCommand command) {
    CommercialDataType dataType = command.dataType();
    String historyKey = dataType.historyKey();

    if (historyRepository.existsByDataTypeAndTargetKeyAndStatus(
        historyKey, command.targetKey(), CommercialDataCollectStatus.RUNNING)) {
      return skipped(command, CommercialDataCollectStatus.RUNNING, "이미 수집이 진행 중입니다.");
    }
    if (!command.force()
        && historyRepository.existsByDataTypeAndTargetKeyAndStatus(
            historyKey, command.targetKey(), CommercialDataCollectStatus.COMPLETED)) {
      return skipped(
          command,
          CommercialDataCollectStatus.COMPLETED,
          "이미 수집한 조건입니다. 다시 수집하려면 force=true로 요청하세요.");
    }

    CommercialDataCollectHistory history = prepareHistory(command);
    long[] progressState = {0L, 0L, 1L};

    try {
      CommercialDataProvider provider = providerRegistry.get(dataType);
      provider.collect(
          command,
          (totalCount, fetchedCount, cursor) -> {
            progressState[0] = totalCount;
            progressState[1] = fetchedCount;
            progressState[2] = cursor;
            history.updateProgress(totalCount, fetchedCount, cursor);
            historyRepository.save(history);
          });

      history.complete(progressState[0], progressState[1]);
      historyRepository.save(history);
      return new CommercialDataCollectResult(
          historyKey,
          command.targetKey(),
          progressState[0],
          progressState[1],
          false,
          CommercialDataCollectStatus.COMPLETED,
          dataType.displayName() + " 수집이 완료되었습니다.");
    } catch (Exception exception) {
      history.fail(progressState[0], progressState[1], progressState[2], resolveMessage(exception));
      historyRepository.save(history);
      log.warn(
          "공공데이터 수집 실패 dataType={} targetKey={} cursor={}",
          dataType,
          command.targetKey(),
          progressState[2],
          exception);

      if (exception instanceof ApplicationException applicationException) {
        throw applicationException;
      }
      throw new ApplicationException(ErrorCode.COMMERCIAL_DATA_PROVIDER_FAILED);
    }
  }

  private CommercialDataCollectHistory prepareHistory(CommercialDataCollectCommand command) {
    String dataType = command.dataType().historyKey();
    if (command.force()) {
      return historyRepository
          .findByDataTypeAndTargetKeyAndStatus(
              dataType, command.targetKey(), CommercialDataCollectStatus.COMPLETED)
          .map(
              history -> {
                history.restart();
                return historyRepository.save(history);
              })
          .orElseGet(
              () ->
                  historyRepository.save(
                      CommercialDataCollectHistory.start(dataType, command.targetKey())));
    }
    return historyRepository.save(
        CommercialDataCollectHistory.start(dataType, command.targetKey()));
  }

  private CommercialDataCollectResult skipped(
      CommercialDataCollectCommand command, CommercialDataCollectStatus status, String message) {
    return new CommercialDataCollectResult(
        command.dataType().historyKey(), command.targetKey(), 0, 0, true, status, message);
  }

  private String resolveMessage(Exception exception) {
    return exception.getMessage() == null || exception.getMessage().isBlank()
        ? "공공데이터 수집 중 알 수 없는 오류가 발생했습니다."
        : exception.getMessage();
  }
}
