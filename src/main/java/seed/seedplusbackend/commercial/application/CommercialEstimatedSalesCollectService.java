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
import seed.seedplusbackend.commercial.infrastructure.client.SeoulCommercialOpenApiProperties;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommercialEstimatedSalesCollectService {

  private static final String DATA_TYPE = "SEOUL_COMMERCIAL_ESTIMATED_SALES";

  private final SeoulCommercialEstimatedSalesClientPort clientPort;
  private final CommercialEstimatedSalesStorePort storePort;
  private final CommercialDataCollectHistoryRepository historyRepository;
  private final SeoulCommercialOpenApiProperties properties;

  public CommercialEstimatedSalesCollectResult collect(
      CommercialEstimatedSalesCollectCommand command) {
    if (isRunning(command)) {
      return new CommercialEstimatedSalesCollectResult(
          DATA_TYPE,
          command.stdrYyquCd(),
          0L,
          0L,
          true,
          CommercialDataCollectStatus.RUNNING,
          "이미 수집이 진행 중인 기준년분기입니다.");
    }

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

    CommercialDataCollectHistory history = prepareHistory(command);

    int startIndex = 1;
    long totalCount = 0L;
    long fetchedCount = 0L;

    try {
      while (true) {
        int endIndex = startIndex + properties.pageSize() - 1;

        CommercialEstimatedSalesPageResult pageResult =
            fetchWithRetry(command.stdrYyquCd(), startIndex, endIndex);

        if (pageResult == null) {
          throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_REQUEST_FAILED);
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
          throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_REQUEST_FAILED);
        }

        startIndex += pageResult.rows().size();
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
    } catch (Exception exception) {
      history.fail(totalCount, fetchedCount, startIndex, resolveErrorMessage(exception));
      historyRepository.save(history);

      log.warn(
          "[CommercialEstimatedSalesCollectService] 작업 실패 기준년분기={} 전체건수={} 수집건수={} 시작인덱스={}",
          command.stdrYyquCd(),
          totalCount,
          fetchedCount,
          startIndex,
          exception);

      if (exception instanceof ApplicationException applicationException) {
        throw applicationException;
      }

      throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_REQUEST_FAILED);
    }
  }

  private boolean isAlreadyCollected(CommercialEstimatedSalesCollectCommand command) {
    return !command.force()
        && historyRepository.existsByDataTypeAndTargetKeyAndStatus(
            DATA_TYPE, command.stdrYyquCd(), CommercialDataCollectStatus.COMPLETED);
  }

  private CommercialEstimatedSalesPageResult fetchWithRetry(
      String stdrYyquCd, int startIndex, int endIndex) {
    for (int retryCount = 0; retryCount <= properties.maxRetryCount(); retryCount++) {
      try {
        return clientPort.fetchByQuarter(stdrYyquCd, startIndex, endIndex);
      } catch (ApplicationException exception) {
        log.warn(
            "[CommercialEstimatedSalesCollectService] 서울시 추정매출 OpenAPI 비재시도 오류 기준년분기={} 시작인덱스={} 종료인덱스={} 재시도횟수={}",
            stdrYyquCd,
            startIndex,
            endIndex,
            retryCount,
            exception);

        throw exception;
      } catch (Exception exception) {
        log.warn(
            "[CommercialEstimatedSalesCollectService] 서울시 추정매출 OpenAPI 요청 실패 기준년분기={} 시작인덱스={} 종료인덱스={} 재시도횟수={}",
            stdrYyquCd,
            startIndex,
            endIndex,
            retryCount,
            exception);

        if (retryCount == properties.maxRetryCount() || !sleepBackoff(retryCount)) {
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
    return ThreadLocalRandom.current()
        .nextLong(properties.minJitterMillis(), properties.maxJitterMillis() + 1);
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

  private boolean isRunning(CommercialEstimatedSalesCollectCommand command) {
    return historyRepository.existsByDataTypeAndTargetKeyAndStatus(
        DATA_TYPE, command.stdrYyquCd(), CommercialDataCollectStatus.RUNNING);
  }

  private CommercialDataCollectHistory prepareHistory(
      CommercialEstimatedSalesCollectCommand command) {
    if (command.force()) {
      return historyRepository
          .findByDataTypeAndTargetKeyAndStatus(
              DATA_TYPE, command.stdrYyquCd(), CommercialDataCollectStatus.COMPLETED)
          .map(
              history -> {
                history.restart();
                return historyRepository.save(history);
              })
          .orElseGet(
              () ->
                  historyRepository.save(
                      CommercialDataCollectHistory.start(DATA_TYPE, command.stdrYyquCd())));
    }

    return historyRepository.save(
        CommercialDataCollectHistory.start(DATA_TYPE, command.stdrYyquCd()));
  }

  private String resolveErrorMessage(Exception exception) {
    if (exception instanceof ApplicationException) {
      return exception.getMessage();
    }

    if (exception.getMessage() == null || exception.getMessage().isBlank()) {
      return "서울시 추정매출 데이터 수집 중 알 수 없는 오류가 발생했습니다.";
    }

    return exception.getMessage();
  }
}
