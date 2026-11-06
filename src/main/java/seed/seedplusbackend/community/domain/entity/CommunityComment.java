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
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import seed.seedplusbackend.community.domain.CommunityContentPolicy;
import seed.seedplusbackend.global.common.BaseTimeEntity;
import seed.seedplusbackend.user.domain.entity.User;

@Getter
@Entity
@Table(name = "community_comments")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityComment extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "community_comment_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private CommunityPost post;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, length = 2000)
  private String content;

  @Column(name = "deleted_at")
  private OffsetDateTime deletedAt;

  public CommunityComment(CommunityPost post, User user, String content) {
    CommunityContentPolicy.requireText(content, 2000);
    this.post = post;
    this.user = user;
    this.content = content;
  }

  public boolean isOwner(Long userId) {
    return userId != null && userId.equals(user.getId());
  }

  public void delete() {
    deletedAt = OffsetDateTime.now();
  }
}
