package seed.seedplusbackend.news.domain;

import java.time.ZoneId;
import java.util.List;
import java.util.regex.Pattern;

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

  public static boolean matchesRegion(String title, String sido, String sigungu) {
    if (title == null || sido == null || sigungu == null || sido.isBlank() || sigungu.isBlank())
      return false;
    String shortSido = sido.replaceFirst("(특별자치시|특별자치도|특별시|광역시|도|시)$", "");
    String sidoNames = Pattern.quote(sido) + "|" + Pattern.quote(shortSido);
    if (sido.endsWith("시")) sidoNames += "|" + Pattern.quote(shortSido + "시");
    // 일부 지역만 적재된 DB에서 이름의 유일성을 추정하지 않고, 명시적인 시도-시군구 쌍만 연결한다.
    String location =
        "(?<![\\p{L}\\p{N}])(?:"
            + sidoNames
            + ")\\s+"
            + Pattern.quote(sigungu)
            + "(?:에서는|에서|에는|은|는|이|가|의|에|을|를|와|과)?(?![\\p{L}\\p{N}])";
    return Pattern.compile(location, Pattern.UNICODE_CHARACTER_CLASS).matcher(title).find();
  }
}
