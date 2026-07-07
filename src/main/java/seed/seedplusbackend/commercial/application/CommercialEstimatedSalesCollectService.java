package seed.seedplusbackend.commercial.application;

import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import seed.seedplusbackend.commercial.application.command.CommercialEstimatedSalesCollectCommand;
import seed.seedplusbackend.commercial.application.port.CommercialEstimatedSalesStorePort;
import seed.seedplusbackend.commercial.application.port.SeoulCommercialEstimatedSalesClientPort;
import seed.seedplusbackend.commercial.application.result.CommercialEstimatedSalesCollectResult;
import seed.seedplusbackend.commercial.application.result.CommercialEstimatedSalesPageResult;
import seed.seedplusbackend.commercial.domain.entity.CommercialDataCollectHistory;
import seed.seedplusbackend.commercial.domain.entity.CommercialDataCollectStatus;
import seed.seedplusbackend.commercial.domain.repository.CommercialDataCollectHistoryRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommercialEstimatedSalesCollectService {

  private static final String DATA_TYPE = "SEOUL_COMMERCIAL_ESTIMATED_SALES";
  private static final int PAGE_SIZE = 1000;
  private static final int MAX_RETRY_COUNT = 3;
  private static final long MIN_JITTER_MILLIS = 300L;
  private static final long MAX_JITTER_MILLIS = 1200L;

  private final SeoulCommercialEstimatedSalesClientPort clientPort;
  private final CommercialEstimatedSalesStorePort storePort;
  private final CommercialDataCollectHistoryRepository historyRepository;

  public CommercialEstimatedSalesCollectResult collect(
      CommercialEstimatedSalesCollectCommand command) {
    if (isAlreadyCollected(command)) {
      return new CommercialEstimatedSalesCollectResult(
          DATA_TYPE,
          command.stdrYyquCd(),
          0L,
          0L,
          true,
          CommercialDataCollectStatus.COMPLETED,
          "이미 수집 완료된 기준년분기입니다. 재수집이 필요하면 force=true로 요청하세요.");
    }

    CommercialDataCollectHistory history =
        CommercialDataCollectHistory.start(DATA_TYPE, command.stdrYyquCd());
    historyRepository.save(history);

    int startIndex = 1;
    long totalCount = 0L;
    long fetchedCount = 0L;

    while (true) {
      int endIndex = startIndex + PAGE_SIZE - 1;

      CommercialEstimatedSalesPageResult pageResult =
          fetchWithRetry(command.stdrYyquCd(), startIndex, endIndex);

      if (pageResult == null) {
        history.fail(totalCount, fetchedCount, startIndex, "서울시 추정매출 OpenAPI 요청 실패");
        historyRepository.save(history);

        log.warn(
            "[CommercialEstimatedSalesCollectService] 작업 실패, 사유=서울시 추정매출 OpenAPI 요청 실패 기준년분기={}",
            command.stdrYyquCd());

        return new CommercialEstimatedSalesCollectResult(
            DATA_TYPE,
            command.stdrYyquCd(),
            totalCount,
            fetchedCount,
            false,
            CommercialDataCollectStatus.FAILED,
            "서울시 추정매출 OpenAPI 요청에 실패했습니다.");
      }

      totalCount = pageResult.totalCount();

      if (pageResult.rows().isEmpty()) {
        break;
      }

      storePort.upsertAll(pageResult.rows());

      fetchedCount += pageResult.rows().size();

      history.updateProgress(totalCount, fetchedCount, startIndex);
      historyRepository.save(history);

      if (fetchedCount >= totalCount) {
        break;
      }

      if (!sleepWithJitter()) {
        history.fail(totalCount, fetchedCount, startIndex, "수집 작업이 중단되었습니다.");
        historyRepository.save(history);

        log.warn(
            "[CommercialEstimatedSalesCollectService] 작업 실패, 사유=수집 작업 중단 기준년분기={}",
            command.stdrYyquCd());

        return new CommercialEstimatedSalesCollectResult(
            DATA_TYPE,
            command.stdrYyquCd(),
            totalCount,
            fetchedCount,
            false,
            CommercialDataCollectStatus.FAILED,
            "수집 작업이 중단되었습니다.");
      }

      startIndex += PAGE_SIZE;
    }

    history.complete(totalCount, fetchedCount);
    historyRepository.save(history);

    log.info(
        "[CommercialEstimatedSalesCollectService] 작업 완료 기준년분기={} 전체건수={} 수집건수={}",
        command.stdrYyquCd(),
        totalCount,
        fetchedCount);

    return new CommercialEstimatedSalesCollectResult(
        DATA_TYPE,
        command.stdrYyquCd(),
        totalCount,
        fetchedCount,
        false,
        CommercialDataCollectStatus.COMPLETED,
        "서울시 상권 추정매출 데이터 수집이 완료되었습니다.");
  }

  private boolean isAlreadyCollected(CommercialEstimatedSalesCollectCommand command) {
    return !command.force()
        && historyRepository.existsByDataTypeAndTargetKeyAndStatus(
            DATA_TYPE, command.stdrYyquCd(), CommercialDataCollectStatus.COMPLETED);
  }

  private CommercialEstimatedSalesPageResult fetchWithRetry(
      String stdrYyquCd, int startIndex, int endIndex) {
    for (int retryCount = 0; retryCount <= MAX_RETRY_COUNT; retryCount++) {
      try {
        return clientPort.fetchByQuarter(stdrYyquCd, startIndex, endIndex);
      } catch (Exception exception) {
        log.warn(
            "[CommercialEstimatedSalesCollectService] 서울시 추정매출 OpenAPI 요청 실패 기준년분기={} 시작인덱스={} 종료인덱스={} 재시도횟수={}",
            stdrYyquCd,
            startIndex,
            endIndex,
            retryCount);

        if (retryCount == MAX_RETRY_COUNT || !sleepBackoff(retryCount)) {
          return null;
        }
      }
    }

    return null;
  }

  private boolean sleepBackoff(int retryCount) {
    long backoffMillis = 500L * (1L << retryCount);
    long jitterMillis = randomJitterMillis();

    return sleep(backoffMillis + jitterMillis);
  }

  private boolean sleepWithJitter() {
    return sleep(randomJitterMillis());
  }

  private long randomJitterMillis() {
    return ThreadLocalRandom.current().nextLong(MIN_JITTER_MILLIS, MAX_JITTER_MILLIS + 1);
  }

  private boolean sleep(long millis) {
    try {
      Thread.sleep(millis);
      return true;
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      return false;
    }
  }
}
