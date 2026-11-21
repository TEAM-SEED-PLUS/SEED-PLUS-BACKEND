package seed.seedplusbackend.news.application.port;

import java.time.*;
import java.util.*;
import org.springframework.data.domain.Page;
import seed.seedplusbackend.news.application.query.NewsSearchQuery;
import seed.seedplusbackend.news.domain.entity.NewsArticle;

public interface NewsReadPort {
  Page<NewsArticle> search(NewsSearchQuery query, OffsetDateTime now);

  Optional<NewsArticle> find(long id, OffsetDateTime now);

  List<NewsArticle> popular(LocalDate from, LocalDate to, OffsetDateTime now);

  Optional<LocalDate> latestDigestDate(LocalDate today, OffsetDateTime now);

  List<NewsArticle> digest(LocalDate date, OffsetDateTime now);
}
