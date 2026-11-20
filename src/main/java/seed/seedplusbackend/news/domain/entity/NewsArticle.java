package seed.seedplusbackend.news.domain.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.news.domain.NewsCategory;
import seed.seedplusbackend.news.domain.NewsSource;
import seed.seedplusbackend.region.domain.entity.Region;

@Getter
@Entity
@Table(name = "news_articles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NewsArticle {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "news_article_id")
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private NewsSource source;

  @Column(name = "source_key", nullable = false, length = 300)
  private String sourceKey;

  @Column(name = "content_type", nullable = false, length = 20)
  private String contentType;

  @Column(name = "display_mode", nullable = false, length = 20)
  private String displayMode;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private NewsCategory category;

  @Column(nullable = false, length = 1000)
  private String title;

  @Column(name = "normalized_title", nullable = false, length = 1000)
  private String normalizedTitle;

  @Column(columnDefinition = "text")
  private String summary;

  @Column(columnDefinition = "text")
  private String body;

  @Column(name = "original_url", nullable = false, length = 2000)
  private String originalUrl;

  @Column(nullable = false, length = 2000)
  private String tags;

  @Column(name = "published_at", nullable = false)
  private OffsetDateTime publishedAt;

  @Column(name = "view_count", nullable = false)
  private long viewCount;

  @Column(nullable = false)
  private boolean active;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "news_article_regions",
      joinColumns = @JoinColumn(name = "article_id"),
      inverseJoinColumns = @JoinColumn(name = "region_id"))
  private Set<Region> regions = new LinkedHashSet<>();

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "news_article_industries",
      joinColumns = @JoinColumn(name = "article_id"),
      inverseJoinColumns = @JoinColumn(name = "industry_id"))
  private Set<Industry> industries = new LinkedHashSet<>();
}
