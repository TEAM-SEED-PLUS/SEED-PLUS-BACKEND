package seed.seedplusbackend.commercial.infrastructure.batch;

import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import seed.seedplusbackend.commercial.application.provider.CommercialDataType;

@Slf4j
@RequiredArgsConstructor
public class PublicDataBatchScheduler implements SchedulingConfigurer {

  private final PublicDataCollectionProperties properties;
  private final PublicDataBatchLauncher launcher;
  private final Clock clock;

  @Override
  public void configureTasks(ScheduledTaskRegistrar registrar) {
    properties
        .crons()
        .forEach(
            (type, cron) -> {
              if ("-".equals(cron)) return;
              registrar.addTriggerTask(() -> run(type), new CronTrigger(cron, properties.zone()));
            });
  }

  private void run(CommercialDataType type) {
    try {
      launcher.launch(type, Instant.now(clock));
    } catch (Exception exception) {
      log.error("공공데이터 배치 실행 실패 source={}", type, exception);
    }
  }
}
