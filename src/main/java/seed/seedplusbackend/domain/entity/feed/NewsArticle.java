package seed.seedplusbackend.domain.entity.feed;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.domain.entity.BaseEntity;
import seed.seedplusbackend.domain.entity.feed.enumerate.NewsCategoryType;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NewsArticle extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NewsCategoryType category;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(nullable = false, length = 2000)
  private String summary;

  @Column(length = 500)
  private String sourceUrl;

  @Column(length = 100)
  private String sourceName;

  @Column(nullable = false)
  private LocalDateTime publishedAt;

  @Column(nullable = false)
  private Long viewCount;
}
