package seed.seedplusbackend.commercial.application.provider;

import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.commercial.application.command.CommercialDataCollectCommand;
import seed.seedplusbackend.commercial.application.command.CommercialEstimatedSalesCollectCommand;
import seed.seedplusbackend.commercial.application.port.CommercialEstimatedSalesStorePort;
import seed.seedplusbackend.commercial.application.port.SeoulCommercialEstimatedSalesClientPort;
import seed.seedplusbackend.commercial.application.result.CommercialEstimatedSalesPageResult;
import seed.seedplusbackend.commercial.infrastructure.client.SeoulCommercialOpenApiProperties;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeoulEstimatedSalesProvider implements CommercialDataProvider {

  private final SeoulCommercialEstimatedSalesClientPort clientPort;
  private final CommercialEstimatedSalesStorePort storePort;
  private final SeoulCommercialOpenApiProperties properties;

  @Override
  public CommercialDataType supports() {
    return CommercialDataType.SEOUL_ESTIMATED_SALES;
  }

  @Override
  public void collect(CommercialDataCollectCommand command, CollectProgress progress) {
    CommercialEstimatedSalesCollectCommand salesCommand = cast(command);
    int startIndex = 1;
    long fetchedCount = 0;

    while (true) {
      int endIndex = startIndex + properties.pageSize() - 1;
      CommercialEstimatedSalesPageResult page =
          fetchWithRetry(salesCommand.stdrYyquCd(), startIndex, endIndex);

      if (page.rows().isEmpty()) {
        progress.update(page.totalCount(), fetchedCount, startIndex);
        return;
      }

      storePort.upsertAll(page.rows());
      fetchedCount += page.rows().size();
      progress.update(page.totalCount(), fetchedCount, startIndex);

      if (fetchedCount >= page.totalCount()) {
        return;
      }
      if (!sleep(randomJitterMillis())) {
        throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_REQUEST_FAILED);
      }
      startIndex += page.rows().size();
    }
  }

  private CommercialEstimatedSalesPageResult fetchWithRetry(
      String quarter, int startIndex, int endIndex) {
    for (int retryCount = 0; retryCount <= properties.maxRetryCount(); retryCount++) {
      try {
        return clientPort.fetchByQuarter(quarter, startIndex, endIndex);
      } catch (ApplicationException exception) {
        if (!isRetryable(exception) || retryCount == properties.maxRetryCount()) {
          throw exception;
        }
        log.warn(
            "서울시 추정매출 요청 재시도 quarter={} start={} retry={}",
            quarter,
            startIndex,
            retryCount + 1,
            exception);
        pauseBeforeRetry(retryCount);
      }
    }
    throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_REQUEST_FAILED);
  }

  private boolean isRetryable(ApplicationException exception) {
    return exception.getErrorCode() == ErrorCode.SEOUL_OPEN_API_REQUEST_FAILED;
  }

  private void pauseBeforeRetry(int retryCount) {
    if (!sleep(500L * (1L << retryCount) + randomJitterMillis())) {
      throw new ApplicationException(ErrorCode.SEOUL_OPEN_API_REQUEST_FAILED);
    }
  }

  private CommercialEstimatedSalesCollectCommand cast(CommercialDataCollectCommand command) {
    if (command instanceof CommercialEstimatedSalesCollectCommand salesCommand) {
      return salesCommand;
    }
    throw new IllegalArgumentException("서울시 추정매출 Provider에 잘못된 요청이 전달되었습니다.");
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
}
