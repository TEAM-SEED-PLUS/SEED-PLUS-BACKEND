package seed.seedplusbackend.news.application;

import java.time.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.global.error.*;
import seed.seedplusbackend.news.application.port.*;
import seed.seedplusbackend.news.domain.NewsPolicy;

@Service
public class NewsViewService {
  private final NewsReadPort read;
  private final NewsOperationsPort operations;
  private final NewsViewerIdentityPort identity;
  private final Clock clock;

  public NewsViewService(
      NewsReadPort read,
      NewsOperationsPort operations,
      NewsViewerIdentityPort identity,
      @Qualifier("newsClock") Clock clock) {
    this.read = read;
    this.operations = operations;
    this.identity = identity;
    this.clock = clock;
  }

  @Transactional
  public String record(long id, Long userId, String cookie) {
    OffsetDateTime now = OffsetDateTime.now(clock);
    read.find(id, now).orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_NEWS));
    var viewer = identity.resolve(userId, cookie);
    operations.recordView(
        id, viewer.viewerHash(), LocalDate.now(clock.withZone(NewsPolicy.ZONE)), now);
    return viewer.newCookie();
  }
}
