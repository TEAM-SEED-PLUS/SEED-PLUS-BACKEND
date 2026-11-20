package seed.seedplusbackend.news.application.port;

import seed.seedplusbackend.news.domain.NewsItem;
import seed.seedplusbackend.news.domain.NewsSource;

public interface NewsPublicationPort {
  long publish(NewsItem item);

  void hide(NewsSource source, String key);
}
