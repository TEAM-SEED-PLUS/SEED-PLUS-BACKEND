package seed.seedplusbackend.news.presentation.dto;

import java.time.OffsetDateTime;
import java.util.List;
import seed.seedplusbackend.news.application.result.NewsArticleResult;

public record NewsArticleResponse(
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
    List<TagResponse> regions,
    List<TagResponse> industries) {
  public record TagResponse(Long id, String name) {}

  public static NewsArticleResponse from(NewsArticleResult r) {
    return new NewsArticleResponse(
        r.id(),
        r.source(),
        r.sourceName(),
        r.contentType(),
        r.displayMode(),
        r.category(),
        r.title(),
        r.summary(),
        r.body(),
        r.originalUrl(),
        r.publishedAt(),
        r.viewCount(),
        r.tags(),
        r.regions().stream().map(t -> new TagResponse(t.id(), t.name())).toList(),
        r.industries().stream().map(t -> new TagResponse(t.id(), t.name())).toList());
  }
}
