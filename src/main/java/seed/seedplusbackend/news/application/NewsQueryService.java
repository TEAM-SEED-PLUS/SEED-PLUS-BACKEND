package seed.seedplusbackend.news.application;

import java.time.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.global.cache.*;
import seed.seedplusbackend.global.error.*;
import seed.seedplusbackend.news.application.port.NewsReadPort;
import seed.seedplusbackend.news.application.query.NewsSearchQuery;
import seed.seedplusbackend.news.application.result.NewsArticleResult;
import seed.seedplusbackend.news.domain.NewsPolicy;

@Service
@Transactional(readOnly = true)
public class NewsQueryService {
  private final NewsReadPort read;
  private final CacheStore cache;
  private final Clock clock;

  public NewsQueryService(
      NewsReadPort read, CacheStore cache, @Qualifier("newsClock") Clock clock) {
    this.read = read;
    this.cache = cache;
    this.clock = clock;
  }

  public Page<NewsArticleResult> search(NewsSearchQuery query) {
    return read.search(query, OffsetDateTime.now(clock)).map(NewsArticleResult::from);
  }

  public NewsArticleResult detail(long id) {
    return NewsArticleResult.from(
        read.find(id, OffsetDateTime.now(clock))
            .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_NEWS)));
  }

  public Page<NewsArticleResult> popular() {
    LocalDate today = LocalDate.now(clock.withZone(NewsPolicy.ZONE));
    PopularSnapshot snapshot =
        cache
            .get(CacheSpec.NEWS_POPULAR, today.toString(), PopularSnapshot.class)
            .orElseGet(
                () -> {
                  var value =
                      new PopularSnapshot(
                          read
                              .popular(today.minusDays(6), today, OffsetDateTime.now(clock))
                              .stream()
                              .map(NewsArticleResult::from)
                              .toList());
                  cache.put(CacheSpec.NEWS_POPULAR, today.toString(), value);
                  return value;
                });
    return new PageImpl<>(snapshot.items(), PageRequest.of(0, 3), snapshot.items().size());
  }

  public Page<NewsArticleResult> digest(LocalDate date) {
    LocalDate selected = date == null ? LocalDate.now(clock.withZone(NewsPolicy.ZONE)) : date;
    var items =
        read.digest(selected, OffsetDateTime.now(clock)).stream()
            .map(NewsArticleResult::from)
            .toList();
    return new PageImpl<>(items, PageRequest.of(0, 12), items.size());
  }

  private record PopularSnapshot(List<NewsArticleResult> items) {}
}
