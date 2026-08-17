package seed.seedplusbackend.commercial.application.provider;

import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.commercial.application.command.CommercialDataCollectCommand;
import seed.seedplusbackend.commercial.application.command.SeoulSdotFootTrafficCollectCommand;
import seed.seedplusbackend.commercial.application.port.SeoulSdotFootTrafficClientPort;
import seed.seedplusbackend.commercial.application.port.SeoulSdotFootTrafficStorePort;
import seed.seedplusbackend.commercial.application.result.SeoulSdotFootTrafficPageResult;
import seed.seedplusbackend.commercial.infrastructure.client.SeoulSdotOpenApiProperties;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeoulSdotFootTrafficProvider implements CommercialDataProvider {

  private final SeoulSdotFootTrafficClientPort clientPort;
  private final SeoulSdotFootTrafficStorePort storePort;
  private final SeoulSdotOpenApiProperties properties;
  private final ExternalApiRetryExecutor retryExecutor;

  @Override
  public CommercialDataType supports() {
    return CommercialDataType.SEOUL_SDOT_FOOT_TRAFFIC;
  }

  @Override
  public void collect(CommercialDataCollectCommand command, CollectProgress progress) {
    cast(command);
    int startIndex = 1;
    long fetchedCount = 0;
    while (true) {
      int endIndex = startIndex + properties.pageSize() - 1;
      SeoulSdotFootTrafficPageResult page = fetchWithRetry(startIndex, endIndex);
      if (page.rows().isEmpty()) {
        progress.update(page.totalCount(), fetchedCount, startIndex);
        return;
      }
      storePort.upsertAll(page.rows());
      fetchedCount += page.rows().size();
      progress.update(page.totalCount(), fetchedCount, startIndex);
      if (endIndex >= page.totalCount()) {
        return;
      }
      startIndex = endIndex + 1;
    }
  }

  private SeoulSdotFootTrafficPageResult fetchWithRetry(int startIndex, int endIndex) {
    return retryExecutor.execute(
        () -> clientPort.fetch(startIndex, endIndex),
        properties.maxRetryCount(),
        exception -> exception.getErrorCode() == ErrorCode.SEOUL_OPEN_API_REQUEST_FAILED,
        retryCount -> 500L * (1L << retryCount) + randomJitterMillis(),
        (retryCount, exception) ->
            log.warn("서울시 S-DoT 요청 재시도 start={} retry={}", startIndex, retryCount + 1, exception),
        exception -> new ApplicationException(ErrorCode.SEOUL_OPEN_API_REQUEST_FAILED, exception));
  }

  private long randomJitterMillis() {
    if (properties.maxJitterMillis() <= properties.minJitterMillis()) {
      return properties.minJitterMillis();
    }
    return ThreadLocalRandom.current()
        .nextLong(properties.minJitterMillis(), properties.maxJitterMillis() + 1);
  }

  private void cast(CommercialDataCollectCommand command) {
    if (!(command instanceof SeoulSdotFootTrafficCollectCommand)) {
      throw new IllegalArgumentException("서울시 S-DoT Provider에 잘못된 요청이 전달되었습니다.");
    }
  }
}
