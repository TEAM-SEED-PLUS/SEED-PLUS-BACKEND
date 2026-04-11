package seed.seedplusbackend.domain.entity.feed;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.domain.entity.BaseEntity;
import seed.seedplusbackend.domain.entity.user.StoreOwner;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_community_reaction_user_post_type", columnNames = {"liker_id", "community_post_id"})
    }
)
public class CommunityLike extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "liker_id", nullable = false)
  private StoreOwner liker;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "community_post_id", nullable = false)
  private CommunityPost communityPost;
}
