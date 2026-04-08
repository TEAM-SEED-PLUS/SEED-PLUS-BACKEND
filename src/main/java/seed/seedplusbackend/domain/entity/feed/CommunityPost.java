package seed.seedplusbackend.domain.entity.feed;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.domain.entity.BaseEntity;
import seed.seedplusbackend.domain.entity.feed.enumerate.CommunityCategoryType;
import seed.seedplusbackend.domain.entity.user.StoreOwner;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CommunityPost extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "author_id", nullable = false)
  private StoreOwner author;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CommunityCategoryType category;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(nullable = false, length = 5000)
  private String content;

  @Column(nullable = false)
  private Long likeCount;

  @Column(nullable = false)
  private Long commentCount;

  @Column(nullable = false)
  private Long shareCount;

  @Column(nullable = false)
  private Boolean deleted;
}