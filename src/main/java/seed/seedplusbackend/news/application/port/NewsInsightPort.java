package seed.seedplusbackend.news.application.port;

import java.util.List;
import seed.seedplusbackend.news.domain.*;

public interface NewsInsightPort {
  List<InsightComparison> candidates(NewsSource source, String after, int limit);
}
