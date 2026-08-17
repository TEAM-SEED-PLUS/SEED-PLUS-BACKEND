package seed.seedplusbackend.commercial.application.provider;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.commercial.application.command.CommercialDataCollectCommand;
import seed.seedplusbackend.commercial.application.command.KosisBusinessCountCollectCommand;
import seed.seedplusbackend.commercial.application.exception.KosisBusinessCountApiRequestException;
import seed.seedplusbackend.commercial.application.port.KosisBusinessCountClientPort;
import seed.seedplusbackend.commercial.application.port.KosisBusinessCountStorePort;
import seed.seedplusbackend.commercial.application.result.KosisBusinessCountRowResult;
import seed.seedplusbackend.commercial.infrastructure.client.KosisBusinessCountOpenApiProperties;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class KosisBusinessCountProvider implements CommercialDataProvider {

  private final KosisBusinessCountClientPort clientPort;
  private final KosisBusinessCountStorePort storePort;
  private final KosisBusinessCountOpenApiProperties properties;
  private final ExternalApiRetryExecutor retryExecutor;

  @Override
  public CommercialDataType supports() {
    return CommercialDataType.KOSIS_BUSINESS_COUNT;
  }

  @Override
  public void collect(CommercialDataCollectCommand command, CollectProgress progress) {
    KosisBusinessCountCollectCommand kosisCommand = cast(command);
    List<KosisBusinessCountRowResult> rows = fetchWithRetry(kosisCommand);

    if (rows.isEmpty()) {
      log.warn("KOSIS 산업별 기업수 응답이 0건입니다. targetKey={}", kosisCommand.targetKey());
      throw new ApplicationException(ErrorCode.KOSIS_OPEN_API_INVALID_RESPONSE);
    }

    storePort.upsertAll(rows);
    progress.update(rows.size(), rows.size(), 1);
  }

  private List<KosisBusinessCountRowResult> fetchWithRetry(
      KosisBusinessCountCollectCommand command) {
    return retryExecutor.execute(
        () -> clientPort.fetch(command),
        properties.maxRetryCount(),
        this::isRetryable,
        retryCount -> properties.initialBackoffMillis() * (1L << retryCount) + randomJitterMillis(),
        (retryCount, exception) ->
            log.warn(
                "KOSIS 산업별 기업수 요청 재시도 targetKey={} retry={}",
                command.targetKey(),
                retryCount + 1,
                exception),
        exception -> new ApplicationException(ErrorCode.KOSIS_OPEN_API_REQUEST_FAILED, exception));
  }

  private boolean isRetryable(ApplicationException exception) {
    if (exception instanceof KosisBusinessCountApiRequestException requestException) {
      return requestException.isRetryable();
    }
    return exception.getErrorCode() == ErrorCode.KOSIS_OPEN_API_REQUEST_FAILED;
  }

  private long randomJitterMillis() {
    if (properties.maxJitterMillis() <= properties.minJitterMillis()) {
      return properties.minJitterMillis();
    }
    return ThreadLocalRandom.current()
        .nextLong(properties.minJitterMillis(), properties.maxJitterMillis() + 1);
  }

  private KosisBusinessCountCollectCommand cast(CommercialDataCollectCommand command) {
    if (command instanceof KosisBusinessCountCollectCommand kosisCommand) {
      return kosisCommand;
    }
    throw new IllegalArgumentException("KOSIS 기업수 Provider에 잘못된 요청이 전달되었습니다.");
  }
}
