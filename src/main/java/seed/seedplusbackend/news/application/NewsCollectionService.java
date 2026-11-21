package seed.seedplusbackend.news.application;

import java.time.*;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import seed.seedplusbackend.global.error.*;
import seed.seedplusbackend.news.application.port.*;
import seed.seedplusbackend.news.domain.*;

@lombok.extern.slf4j.Slf4j
@Service
public class NewsCollectionService {
  private final NewsFeedPort feed;
  private final NewsIngestionPort ingestion;
  private final NewsPublicationPort publication;
  private final NewsJobLockPort locks;
  private final NewsTelemetryPort telemetry;
  private final Clock clock;

  public NewsCollectionService(
      NewsFeedPort feed,
      NewsIngestionPort ingestion,
      NewsPublicationPort publication,
      NewsJobLockPort locks,
      NewsTelemetryPort telemetry,
      @Qualifier("newsClock") Clock clock) {
    this.feed = feed;
    this.ingestion = ingestion;
    this.publication = publication;
    this.locks = locks;
    this.telemetry = telemetry;
    this.clock = clock;
  }

  public void collect(NewsSource source) {
    if (!source.rss()) throw new ApplicationException(ErrorCode.INVALID_NEWS_ITEM);
    UUID owner = UUID.randomUUID();
    if (!locks.acquire(source.name(), owner)) {
      telemetry.count(source.name(), "locked");
      return;
    }
    RuntimeException fetchFailure = null;
    try {
      try {
        var items = feed.fetch(source);
        locks.owned(
            source.name(),
            owner,
            () -> {
              ingestion.stage(source, items);
              ingestion.sourceSucceeded(source);
            });
      } catch (RuntimeException e) {
        if (e instanceof ApplicationException a && a.getErrorCode() == ErrorCode.NEWS_LEASE_LOST)
          throw e;
        fetchFailure = e;
        locks.owned(source.name(), owner, () -> ingestion.sourceFailed(source));
        telemetry.count(source.name(), "fetch_failed");
      }
      // 소스 장애 중에도 이전에 저장된 재시도 항목은 처리한다.
      while (true) {
        var pending = ingestion.pending(source);
        if (pending.isEmpty()) break;
        for (var row : pending) process(source, owner, row);
      }
      if (fetchFailure != null) throw fetchFailure;
    } finally {
      locks.release(source.name(), owner);
    }
  }

  private void process(NewsSource source, UUID owner, NewsIngestionPort.Pending row) {
    final NewsItem item;
    try {
      item =
          RssNewsNormalizer.normalize(
              source, row.title(), row.url(), row.publishedText(), OffsetDateTime.now(clock));
    } catch (DomainException e) {
      locks.owned(source.name(), owner, () -> ingestion.complete(row.id(), "INVALID", null, null));
      telemetry.count(source.name(), "invalid");
      return;
    }
    try {
      if (!NewsPolicy.relevant(item.title())) {
        locks.owned(
            source.name(),
            owner,
            () -> {
              publication.hide(source, item.sourceKey());
              ingestion.complete(row.id(), "SKIPPED", null, null);
            });
        telemetry.count(source.name(), "skipped");
        return;
      }
      locks.owned(
          source.name(),
          owner,
          () -> {
            long id = publication.publish(item);
            ingestion.complete(row.id(), "PUBLISHED", id, ingestion.duplicateCandidate(item, id));
          });
      telemetry.count(source.name(), "published");
      telemetry.lag(
          source.name(),
          Math.max(0, Duration.between(row.createdAt().toInstant(), clock.instant()).toSeconds()));
    } catch (RuntimeException e) {
      if (e instanceof ApplicationException a && a.getErrorCode() == ErrorCode.NEWS_LEASE_LOST)
        throw e;
      log.warn(
          "뉴스 항목 반영 실패 source={} itemId={} attempt={}", source, row.id(), row.attempts() + 1, e);
      locks.owned(source.name(), owner, () -> ingestion.fail(row.id(), "PUBLICATION_FAILED"));
      telemetry.count(source.name(), row.attempts() >= 3 ? "failed" : "retry");
    }
  }
}
