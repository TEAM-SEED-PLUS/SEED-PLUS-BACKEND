package seed.seedplusbackend.commercial.application.provider;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.commercial.application.command.CommercialDataCollectCommand;
import seed.seedplusbackend.commercial.application.command.KosisBusinessSurvivalCollectCommand;
import seed.seedplusbackend.commercial.application.port.KosisBusinessSurvivalClientPort;
import seed.seedplusbackend.commercial.application.port.KosisBusinessSurvivalStorePort;
import seed.seedplusbackend.commercial.application.result.KosisBusinessSurvivalRowResult;
import seed.seedplusbackend.commercial.infrastructure.client.KosisBusinessSurvivalOpenApiProperties;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class KosisBusinessSurvivalProvider implements CommercialDataProvider {

  private final KosisBusinessSurvivalClientPort clientPort;
  private final KosisBusinessSurvivalStorePort storePort;
  private final KosisBusinessSurvivalOpenApiProperties properties;
  private final ExternalApiRetryExecutor retryExecutor;

  @Override
  public CommercialDataType supports() {
    return CommercialDataType.KOSIS_BUSINESS_SURVIVAL_RATE;
  }

  @Override
  public void collect(CommercialDataCollectCommand command, CollectProgress progress) {
    KosisBusinessSurvivalCollectCommand kosisCommand = cast(command);
    List<KosisBusinessSurvivalRowResult> rows = fetchWithRetry(kosisCommand);

    if (rows.isEmpty()) {
      log.warn("KOSIS 신생기업 생존율 응답이 0건입니다. targetKey={}", kosisCommand.targetKey());
      throw new ApplicationException(ErrorCode.KOSIS_OPEN_API_INVALID_RESPONSE);
    }

    storePort.upsertAll(rows);
    progress.update(rows.size(), rows.size(), 1);
  }

  private List<KosisBusinessSurvivalRowResult> fetchWithRetry(
      KosisBusinessSurvivalCollectCommand command) {
    return retryExecutor.execute(
        () -> clientPort.fetch(command),
        properties.maxRetryCount(),
        this::isRetryable,
        retryCount -> properties.initialBackoffMillis() * (1L << retryCount) + randomJitterMillis(),
        (retryCount, exception) ->
            log.warn(
                "KOSIS 신생기업 생존율 요청 재시도 targetKey={} retry={}",
                command.targetKey(),
                retryCount + 1,
                exception),
        exception -> new ApplicationException(ErrorCode.KOSIS_OPEN_API_REQUEST_FAILED, exception));
  }

  private boolean isRetryable(ApplicationException exception) {
    return exception.getErrorCode() == ErrorCode.KOSIS_OPEN_API_REQUEST_FAILED;
  }

  private long randomJitterMillis() {
    if (properties.maxJitterMillis() <= properties.minJitterMillis()) {
      return properties.minJitterMillis();
    }
    return ThreadLocalRandom.current()
        .nextLong(properties.minJitterMillis(), properties.maxJitterMillis() + 1);
  }

  private KosisBusinessSurvivalCollectCommand cast(CommercialDataCollectCommand command) {
    if (command instanceof KosisBusinessSurvivalCollectCommand kosisCommand) {
      return kosisCommand;
    }
    throw new IllegalArgumentException("KOSIS 생존율 Provider에 잘못된 요청이 전달되었습니다.");
  }
}
