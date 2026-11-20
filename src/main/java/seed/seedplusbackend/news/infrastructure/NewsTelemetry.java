package seed.seedplusbackend.news.infrastructure;

import io.micrometer.core.instrument.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.news.application.port.NewsTelemetryPort;
import seed.seedplusbackend.news.domain.NewsSource;

@Component
public class NewsTelemetry implements NewsTelemetryPort {
  private final MeterRegistry registry;

  public NewsTelemetry(MeterRegistry registry, JdbcTemplate jdbc) {
    this.registry = registry;
    for (NewsSource source : NewsSource.values())
      if (source.rss()) {
        Gauge.builder(
                "news.source.last.success.timestamp",
                () -> {
                  Double value =
                      jdbc.queryForObject(
                          "SELECT coalesce(max(extract(epoch from last_success_at)),0) FROM news_source_states WHERE source=?",
                          Double.class,
                          source.name());
                  return value == null ? 0 : value;
                })
            .tag("source", source.name())
            .register(registry);
        for (String status : new String[] {"PENDING", "RETRY", "FAILED", "INVALID", "SKIPPED"}) {
          Gauge.builder(
                  "news.ingestion.items",
                  () ->
                      jdbc.queryForObject(
                          "SELECT count(*) FROM news_ingestion_items WHERE source=? AND status=?",
                          Long.class,
                          source.name(),
                          status))
              .tag("source", source.name())
              .tag("status", status)
              .register(registry);
        }
      }
  }

  @Override
  public void count(String source, String outcome) {
    registry.counter("news.processing", "source", source, "outcome", outcome).increment();
  }

  @Override
  public void lag(String source, double seconds) {
    DistributionSummary.builder("news.processing.lag.seconds")
        .tag("source", source)
        .register(registry)
        .record(seconds);
  }
}
