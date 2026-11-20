package seed.seedplusbackend.news.domain;

import java.time.ZoneId;
import java.util.List;

public final class NewsPolicy {
  public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
  public static final String RULE_VERSION = "v1";
  public static final List<String> KEYWORDS =
      List.of("소상공인", "소공인", "자영업", "창업", "상권", "전통시장", "스타트업", "벤처", "중소기업");

  private NewsPolicy() {}

  public static List<String> tags(String title) {
    return KEYWORDS.stream().filter(title::contains).toList();
  }

  public static boolean relevant(String title) {
    return !tags(title).isEmpty();
  }
}
