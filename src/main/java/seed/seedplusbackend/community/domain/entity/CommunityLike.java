package seed.seedplusbackend.community.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.global.common.BaseCreatedEntity;
import seed.seedplusbackend.user.domain.entity.User;

@Getter
@Entity
@Table(
    name = "community_likes",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_community_likes",
            columnNames = {"post_id", "user_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityLike extends BaseCreatedEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "community_like_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private CommunityPost post;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  public CommunityLike(CommunityPost post, User user) {
    this.post = post;
    this.user = user;
  }
}
