package seed.seedplusbackend.commercial.application.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.commercial.application.command.CommercialDataCollectCommand;
import seed.seedplusbackend.commercial.application.command.SmallBusinessStoreCollectCommand;
import seed.seedplusbackend.commercial.application.exception.SmallBusinessStoreApiRequestException;
import seed.seedplusbackend.commercial.application.port.SmallBusinessStoreClientPort;
import seed.seedplusbackend.commercial.application.port.SmallBusinessStoreStorePort;
import seed.seedplusbackend.commercial.application.result.SmallBusinessStorePageResult;
import seed.seedplusbackend.commercial.infrastructure.client.SmallBusinessStoreOpenApiProperties;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmallBusinessStoreProvider implements CommercialDataProvider {

  private static final long MAX_BACKOFF_MILLIS = 30_000L;

  private final SmallBusinessStoreClientPort clientPort;
  private final SmallBusinessStoreStorePort storePort;
  private final SmallBusinessStoreOpenApiProperties properties;

  @Override
  public CommercialDataType supports() {
    return CommercialDataType.SMALL_BUSINESS_STORE;
  }

  @Override
  public void collect(CommercialDataCollectCommand command, CollectProgress progress) {
    SmallBusinessStoreCollectCommand storeCommand = cast(command);
    int pageNumber = 1;
    long fetchedCount = 0;

    while (true) {
      SmallBusinessStorePageResult page = fetchWithRetry(storeCommand, pageNumber);

      if (page.rows().isEmpty()) {
        progress.update(page.totalCount(), fetchedCount, pageNumber);
        return;
      }

      storePort.upsertAll(storeCommand.commercialAreaCode(), page.rows());
      fetchedCount += page.rows().size();
      progress.update(page.totalCount(), fetchedCount, pageNumber);

      if (fetchedCount >= page.totalCount()) {
        return;
      }
      pageNumber++;
    }
  }

  private SmallBusinessStorePageResult fetchWithRetry(
      SmallBusinessStoreCollectCommand command, int pageNumber) {
    for (int retryCount = 0; retryCount <= properties.maxRetryCount(); retryCount++) {
      try {
        return clientPort.fetch(command, pageNumber, properties.pageSize());
      } catch (ApplicationException exception) {
        if (!isRetryable(exception) || retryCount == properties.maxRetryCount()) {
          throw exception;
        }
        logRetry(command, pageNumber, retryCount, exception);
      }

      pause(backoffMillis(retryCount));
    }

    throw new ApplicationException(ErrorCode.SMALL_BUSINESS_STORE_API_REQUEST_FAILED);
  }

  private boolean isRetryable(ApplicationException exception) {
    if (exception instanceof SmallBusinessStoreApiRequestException requestException) {
      return requestException.isRetryable();
    }
    return exception.getErrorCode() == ErrorCode.SMALL_BUSINESS_STORE_API_REQUEST_FAILED;
  }

  private void logRetry(
      SmallBusinessStoreCollectCommand command,
      int pageNumber,
      int retryCount,
      RuntimeException exception) {
    log.warn(
        "소상공인 상가정보 요청 재시도 commercialAreaCode={} page={} retry={}",
        command.commercialAreaCode(),
        pageNumber,
        retryCount + 1,
        exception);
  }

  private long backoffMillis(int retryCount) {
    long exponentialBackoff = properties.initialBackoffMillis() * (1L << retryCount);
    return Math.min(exponentialBackoff, MAX_BACKOFF_MILLIS);
  }

  private void pause(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new ApplicationException(ErrorCode.SMALL_BUSINESS_STORE_API_REQUEST_FAILED, exception);
    }
  }

  private SmallBusinessStoreCollectCommand cast(CommercialDataCollectCommand command) {
    if (command instanceof SmallBusinessStoreCollectCommand storeCommand) {
      return storeCommand;
    }
    throw new IllegalArgumentException("소상공인 상가정보 Provider에 잘못된 요청이 전달되었습니다.");
  }
}
