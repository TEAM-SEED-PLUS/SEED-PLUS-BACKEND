package seed.seedplusbackend.news.application.port;

import java.time.*;

public interface NewsOperationsPort {
  boolean recordView(long articleId, String viewerHash, LocalDate date, OffsetDateTime now);

  void createDigest(LocalDate date);

  void pruneViews(LocalDate before);
}
