package seed.seedplusbackend.analysis.application;

import java.time.Duration;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.commercial.application.command.CommercialDataCollectCommand;
import seed.seedplusbackend.commercial.application.result.CommercialDataCollectResult;
import seed.seedplusbackend.commercial.domain.entity.CommercialDataCollectHistory;
import seed.seedplusbackend.commercial.domain.entity.CommercialDataCollectStatus;
import seed.seedplusbackend.commercial.domain.repository.CommercialDataCollectHistoryRepository;

@Component
public class CommercialDataCollectionWaiter {

  private static final String TIMEOUT_ERROR_MESSAGE = "동일한 공공데이터 수집이 제한 시간 내에 완료되지 않아 중단 처리되었습니다.";

  private final CommercialDataCollectHistoryRepository historyRepository;
  private final Duration timeout;
  private final Duration pollInterval;

  public CommercialDataCollectionWaiter(
      CommercialDataCollectHistoryRepository historyRepository,
      @Value("${analysis.data-collection.wait-timeout:10m}") Duration timeout,
      @Value("${analysis.data-collection.poll-interval:1s}") Duration pollInterval) {
    this.historyRepository = historyRepository;
    this.timeout = timeout;
    this.pollInterval = pollInterval;
  }

  public CommercialDataCollectResult awaitCompletion(CommercialDataCollectCommand command) {
    long deadline = System.nanoTime() + timeout.toNanos();
    while (System.nanoTime() < deadline) {
      CommercialDataCollectHistory history = findLatest(command);
      if (history.getStatus() != CommercialDataCollectStatus.RUNNING) {
        return toResult(history);
      }
      if (isStale(history)) {
        failStaleHistory(history);
      }
      sleep();
    }

    CommercialDataCollectHistory history = findLatest(command);
    if (history.getStatus() != CommercialDataCollectStatus.RUNNING) {
      return toResult(history);
    }
    failStaleHistory(history);
    throw new IllegalStateException(TIMEOUT_ERROR_MESSAGE);
  }

  private boolean isStale(CommercialDataCollectHistory history) {
    OffsetDateTime lastActivityAt =
        history.getUpdatedAt() == null ? history.getStartedAt() : history.getUpdatedAt();
    return !lastActivityAt.plus(timeout).isAfter(OffsetDateTime.now());
  }

  private void failStaleHistory(CommercialDataCollectHistory history) {
    history.fail(
        history.getTotalCount(),
        history.getFetchedCount(),
        history.getLastStartIndex(),
        TIMEOUT_ERROR_MESSAGE);
    historyRepository.save(history);
    throw new IllegalStateException(TIMEOUT_ERROR_MESSAGE);
  }

  private CommercialDataCollectHistory findLatest(CommercialDataCollectCommand command) {
    return historyRepository
        .findTopByDataTypeAndTargetKeyOrderByStartedAtDesc(
            command.dataType().historyKey(), command.targetKey())
        .orElseThrow(() -> new IllegalStateException("진행 중인 공공데이터 수집 이력을 찾을 수 없습니다."));
  }

  private CommercialDataCollectResult toResult(CommercialDataCollectHistory history) {
    String message =
        history.getStatus() == CommercialDataCollectStatus.COMPLETED
            ? "기존 공공데이터 수집이 완료되었습니다."
            : history.getErrorMessage();
    return new CommercialDataCollectResult(
        history.getDataType(),
        history.getTargetKey(),
        history.getTotalCount(),
        history.getFetchedCount(),
        true,
        history.getStatus(),
        message);
  }

  private void sleep() {
    try {
      Thread.sleep(pollInterval.toMillis());
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("공공데이터 수집 완료 대기 중 요청이 중단되었습니다.", exception);
    }
  }
}
