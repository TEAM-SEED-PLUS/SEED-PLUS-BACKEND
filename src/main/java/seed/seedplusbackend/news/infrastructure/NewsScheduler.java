package seed.seedplusbackend.news.infrastructure;

import jakarta.annotation.PreDestroy;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.news.domain.NewsPolicy;

@Slf4j
@Component
@ConditionalOnProperty(name = "news.batch.enabled", havingValue = "true")
public class NewsScheduler {
  private final NewsJobRunner runner;
  private final Map<String, String> crons;
  // 공공데이터의 장시간 수집이 뉴스 스케줄을 막거나 기존 배치의 동시성을 바꾸지 않는다.
  private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
  private boolean started;

  public NewsScheduler(
      NewsJobRunner runner,
      @Value("${news.batch.press-cron:0 */10 * * * *}") String press,
      @Value("${news.batch.announcement-cron:30 */10 * * * *}") String announcements,
      @Value("${news.batch.insight-cron:0 0 6 * * *}") String insights,
      @Value("${news.batch.digest-cron:0 0 9 * * *}") String digest) {
    this.runner = runner;
    crons =
        Map.of(
            "MSS_PRESS",
            press,
            "MSS_ANNOUNCEMENT",
            announcements,
            "INSIGHTS",
            insights,
            "DIGEST",
            digest);
  }

  @EventListener(ApplicationReadyEvent.class)
  public synchronized void start() {
    if (started) return;
    started = true;
    scheduler.setPoolSize(4);
    scheduler.setThreadNamePrefix("news-scheduler-");
    scheduler.initialize();
    crons.forEach(
        (operation, cron) -> {
          if (!"-".equals(cron))
            scheduler.schedule(() -> run(operation), new CronTrigger(cron, NewsPolicy.ZONE));
        });
  }

  @PreDestroy
  public synchronized void stop() {
    if (started) scheduler.shutdown();
  }

  private void run(String operation) {
    try {
      runner.run(operation);
    } catch (Exception e) {
      log.error("뉴스 배치 실패 operation={}", operation, e);
    }
  }
}
