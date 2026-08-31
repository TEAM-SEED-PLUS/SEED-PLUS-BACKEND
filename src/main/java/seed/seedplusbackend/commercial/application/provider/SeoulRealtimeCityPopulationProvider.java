package seed.seedplusbackend.commercial.application.provider;

import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.commercial.application.command.CommercialDataCollectCommand;
import seed.seedplusbackend.commercial.application.command.SeoulRealtimeCityPopulationCollectCommand;
import seed.seedplusbackend.commercial.application.port.SeoulRealtimeCityPopulationClientPort;
import seed.seedplusbackend.commercial.application.port.SeoulRealtimeCityPopulationStorePort;
import seed.seedplusbackend.commercial.application.result.SeoulRealtimeCityPopulationResult;
import seed.seedplusbackend.commercial.infrastructure.client.SeoulRealtimeCityOpenApiProperties;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeoulRealtimeCityPopulationProvider implements CommercialDataProvider {

  private final SeoulRealtimeCityPopulationClientPort clientPort;
  private final SeoulRealtimeCityPopulationStorePort storePort;
  private final SeoulRealtimeCityOpenApiProperties properties;
  private final ExternalApiRetryExecutor retryExecutor;

  @Override
  public CommercialDataType supports() {
    return CommercialDataType.SEOUL_REALTIME_CITY_POPULATION;
  }

  @Override
  public void collect(CommercialDataCollectCommand command, CollectProgress progress) {
    SeoulRealtimeCityPopulationCollectCommand realtimeCommand = cast(command);
    SeoulRealtimeCityPopulationResult result = fetchWithRetry(realtimeCommand.area());
    storePort.upsert(result);
    progress.update(1, 1, 1);
  }

  private SeoulRealtimeCityPopulationResult fetchWithRetry(String area) {
    return retryExecutor.execute(
        () -> clientPort.fetch(area),
        properties.maxRetryCount(),
        exception -> exception.getErrorCode() == ErrorCode.SEOUL_OPEN_API_REQUEST_FAILED,
        retryCount -> properties.initialBackoffMillis() * (1L << retryCount) + randomJitterMillis(),
        (retryCount, exception) ->
            log.warn("서울시 실시간 도시 인구 요청 재시도 area={} retry={}", area, retryCount + 1, exception),
        exception -> new ApplicationException(ErrorCode.SEOUL_OPEN_API_REQUEST_FAILED, exception));
  }

  private long randomJitterMillis() {
    if (properties.maxJitterMillis() <= properties.minJitterMillis()) {
      return properties.minJitterMillis();
    }
    return ThreadLocalRandom.current()
        .nextLong(properties.minJitterMillis(), properties.maxJitterMillis() + 1);
  }

  private SeoulRealtimeCityPopulationCollectCommand cast(CommercialDataCollectCommand command) {
    if (command instanceof SeoulRealtimeCityPopulationCollectCommand realtimeCommand) {
      return realtimeCommand;
    }
    throw new IllegalArgumentException("서울시 실시간 도시 인구 Provider에 잘못된 요청이 전달되었습니다.");
  }
}
