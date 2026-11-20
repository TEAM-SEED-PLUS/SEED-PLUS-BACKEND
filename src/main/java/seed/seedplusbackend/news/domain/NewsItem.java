package seed.seedplusbackend.news.domain;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import seed.seedplusbackend.global.error.DomainException;
import seed.seedplusbackend.global.error.ErrorCode;

public record NewsItem(
    NewsSource source,
    String sourceKey,
    String title,
    String summary,
    String body,
    String originalUrl,
    OffsetDateTime publishedAt,
    NewsCategory category,
    String tags,
    List<Long> regionIds,
    List<Long> industryIds) {
  public NewsItem {
    if (source == null
        || sourceKey == null
        || sourceKey.isBlank()
        || sourceKey.length() > 300
        || title == null
        || title.isBlank()
        || title.length() > 1000
        || publishedAt == null
        || category == null
        || originalUrl == null
        || originalUrl.length() > 2000) invalid();
    try {
      URI uri = URI.create(originalUrl);
      if (!"https".equals(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null)
        invalid();
    } catch (IllegalArgumentException e) {
      invalid();
    }
    if (source.rss() && (summary != null || body != null)) invalid();
    if (!source.rss() && (summary == null || body == null)) invalid();
    title = title.trim();
    tags = tags == null ? "" : tags;
    if (tags.length() > 2000) invalid();
    regionIds = List.copyOf(regionIds == null ? List.of() : regionIds);
    industryIds = List.copyOf(industryIds == null ? List.of() : industryIds);
  }

  public String contentType() {
    return source.rss() ? "NEWS" : "INSIGHT";
  }

  public String displayMode() {
    return source.rss() ? "OUTLINK" : "INTERNAL";
  }

  public String normalizedTitle() {
    return title.replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
  }

  private static void invalid() {
    throw new DomainException(ErrorCode.INVALID_NEWS_ITEM);
  }
}
