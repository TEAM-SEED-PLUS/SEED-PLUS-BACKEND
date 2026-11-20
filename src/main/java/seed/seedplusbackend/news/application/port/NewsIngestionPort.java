package seed.seedplusbackend.news.application.port;

import java.time.OffsetDateTime;
import java.util.List;
import seed.seedplusbackend.news.domain.*;

public interface NewsIngestionPort {
  void stage(NewsSource source, List<NewsFeedPort.RawItem> items);

  List<Pending> pending(NewsSource source);

  void complete(long id, String status, Long articleId, Long duplicateId);

  void fail(long id, String code);

  Long duplicateCandidate(NewsItem item, long articleId);

  void sourceSucceeded(NewsSource source);

  void sourceFailed(NewsSource source);

  record Pending(
      long id,
      String title,
      String url,
      String publishedText,
      int attempts,
      OffsetDateTime createdAt) {}
}
