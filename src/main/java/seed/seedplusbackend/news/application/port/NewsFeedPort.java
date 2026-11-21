package seed.seedplusbackend.news.application.port;

import java.util.List;
import seed.seedplusbackend.news.domain.NewsSource;

public interface NewsFeedPort {
  List<RawItem> fetch(NewsSource source);

  record RawItem(String title, String url, String publishedText) {}
}
