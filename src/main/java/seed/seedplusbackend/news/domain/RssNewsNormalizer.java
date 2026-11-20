package seed.seedplusbackend.news.domain;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.*;
import java.util.*;
import seed.seedplusbackend.global.error.*;

public final class RssNewsNormalizer {
  private static final DateTimeFormatter DATE =
      DateTimeFormatter.ofPattern("uuuuMMddHHmmss").withResolverStyle(ResolverStyle.STRICT);

  private RssNewsNormalizer() {}

  public static NewsItem normalize(
      NewsSource source, String title, String url, String date, OffsetDateTime now) {
    try {
      if (!source.rss()) throw new IllegalArgumentException();
      URI uri = URI.create(url);
      if (!"https".equals(uri.getScheme())
          || !Set.of("mss.go.kr", "www.mss.go.kr").contains(uri.getHost())
          || uri.getUserInfo() != null
          || (uri.getPort() != -1 && uri.getPort() != 443)
          || !"/site/smba/ex/bbs/View.do".equals(uri.getPath()))
        throw new IllegalArgumentException();
      Map<String, String> params = new HashMap<>();
      for (String part : uri.getRawQuery().split("&")) {
        String[] pair = part.split("=", 2);
        if (pair.length != 2) continue;
        String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
        if (params.putIfAbsent(key, URLDecoder.decode(pair[1], StandardCharsets.UTF_8)) != null)
          throw new IllegalArgumentException();
      }
      String id = params.get("bcIdx");
      if (!String.valueOf(source.boardId()).equals(params.get("cbIdx"))
          || id == null
          || !id.matches("[1-9][0-9]{0,19}")) throw new IllegalArgumentException();
      OffsetDateTime published =
          LocalDateTime.parse(date, DATE).atZone(NewsPolicy.ZONE).toOffsetDateTime();
      if (published.isAfter(now.plusMinutes(5))) throw new IllegalArgumentException();
      return new NewsItem(
          source,
          source.boardId() + ":" + id,
          title,
          null,
          null,
          "https://www.mss.go.kr/site/smba/ex/bbs/View.do?cbIdx="
              + source.boardId()
              + "&bcIdx="
              + id,
          published,
          NewsCategory.POLICY_FINANCE,
          String.join(",", NewsPolicy.tags(title)),
          List.of(),
          List.of());
    } catch (IllegalArgumentException | DateTimeException | NullPointerException e) {
      throw new DomainException(ErrorCode.INVALID_NEWS_ITEM);
    }
  }
}
