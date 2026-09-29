package seed.seedplusbackend.commercial.infrastructure.batch;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.commercial.application.provider.CommercialDataType;

@Component
@RequiredArgsConstructor
public class PublicDataCollectionSchedule {

  private final PublicDataCollectionProperties properties;

  public Optional<Instant> nextExecution(CommercialDataType type, Instant after) {
    String cron = properties.crons().get(type);
    if (cron == null || "-".equals(cron)) {
      return Optional.empty();
    }
    ZonedDateTime next = CronExpression.parse(cron).next(after.atZone(properties.zone()));
    return Optional.ofNullable(next).map(ZonedDateTime::toInstant);
  }
}
