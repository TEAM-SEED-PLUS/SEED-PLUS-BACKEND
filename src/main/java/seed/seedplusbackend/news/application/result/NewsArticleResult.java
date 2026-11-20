package seed.seedplusbackend.news.application.result;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import seed.seedplusbackend.news.domain.entity.NewsArticle;

public record NewsArticleResult(
    Long id,
    String source,
    String sourceName,
    String contentType,
    String displayMode,
    String category,
    String title,
    String summary,
    String body,
    String originalUrl,
    OffsetDateTime publishedAt,
    long viewCount,
    List<String> tags,
    List<Tag> regions,
    List<Tag> industries) {
  public record Tag(Long id, String name) {}

  public static NewsArticleResult from(NewsArticle a) {
    boolean internal = "INTERNAL".equals(a.getDisplayMode());
    return new NewsArticleResult(
        a.getId(),
        a.getSource().name(),
        a.getSource().label(),
        a.getContentType(),
        a.getDisplayMode(),
        a.getCategory().name(),
        a.getTitle(),
        internal ? a.getSummary() : null,
        internal ? a.getBody() : null,
        a.getOriginalUrl(),
        a.getPublishedAt(),
        a.getViewCount(),
        a.getTags().isBlank() ? List.of() : List.of(a.getTags().split(",")),
        a.getRegions().stream()
            .map(r -> new Tag(r.getId(), r.getSigungu()))
            .sorted(Comparator.comparing(Tag::id))
            .toList(),
        a.getIndustries().stream()
            .map(i -> new Tag(i.getId(), i.getName()))
            .sorted(Comparator.comparing(Tag::id))
            .toList());
  }
}
