package seed.seedplusbackend.news.application;

import java.time.*;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import seed.seedplusbackend.news.application.port.*;
import seed.seedplusbackend.news.domain.*;

@Service
public class NewsMaintenanceService {
  private final NewsJobLockPort locks;
  private final NewsInsightPort insights;
  private final NewsPublicationPort publication;
  private final NewsOperationsPort operations;
  private final NewsTelemetryPort telemetry;
  private final Clock clock;

  public NewsMaintenanceService(
      NewsJobLockPort locks,
      NewsInsightPort insights,
      NewsPublicationPort publication,
      NewsOperationsPort operations,
      NewsTelemetryPort telemetry,
      @Qualifier("newsClock") Clock clock) {
    this.locks = locks;
    this.insights = insights;
    this.publication = publication;
    this.operations = operations;
    this.telemetry = telemetry;
    this.clock = clock;
  }

  public void insights() {
    UUID owner = UUID.randomUUID();
    if (!locks.acquire("INSIGHTS", owner)) return;
    try {
      for (NewsSource source : new NewsSource[] {NewsSource.SEOUL_SALES, NewsSource.REB_RENT}) {
        String cursor = "";
        while (true) {
          var rows = insights.candidates(source, cursor, 100);
          if (rows.isEmpty()) break;
          for (var row : rows) {
            locks.owned(
                "INSIGHTS",
                owner,
                () -> {
                  if (row.changed()) publication.publish(row.toItem(OffsetDateTime.now(clock)));
                  else if (row.comparable()) publication.hide(source, row.key());
                });
            telemetry.count(source.name(), row.changed() ? "published" : "skipped");
          }
          cursor = rows.getLast().key();
        }
      }
    } finally {
      locks.release("INSIGHTS", owner);
    }
  }

  public void digest(LocalDate date) {
    UUID owner = UUID.randomUUID();
    if (!locks.acquire("DIGEST", owner)) return;
    try {
      locks.owned(
          "DIGEST",
          owner,
          () -> {
            operations.createDigest(date);
            operations.pruneViews(LocalDate.now(clock.withZone(NewsPolicy.ZONE)).minusDays(89));
          });
    } finally {
      locks.release("DIGEST", owner);
    }
  }
}
