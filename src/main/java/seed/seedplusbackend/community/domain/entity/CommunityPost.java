package seed.seedplusbackend.community.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import seed.seedplusbackend.community.domain.CommunityContentPolicy;
import seed.seedplusbackend.global.common.BaseTimeEntity;
import seed.seedplusbackend.global.error.DomainException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.industry.domain.entity.IndustryStatus;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.domain.entity.RegionCodeType;
import seed.seedplusbackend.user.domain.entity.User;

@Getter
@Entity
@Table(name = "community_posts")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityPost extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "community_post_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, length = 100)
  private String title;

  @Column(nullable = false, length = 10000)
  private String content;

  @Column(length = 200)
  private String location;

  @Column(name = "like_count", nullable = false)
  private long likeCount;

  @Column(name = "comment_count", nullable = false)
  private long commentCount;

  @Column(name = "deleted_at")
  private OffsetDateTime deletedAt;

  @Getter(AccessLevel.NONE)
  @ManyToMany
  @JoinTable(
      name = "community_post_regions",
      joinColumns = @JoinColumn(name = "post_id"),
      inverseJoinColumns = @JoinColumn(name = "region_id"))
  private Set<Region> regions = new LinkedHashSet<>();

  @Getter(AccessLevel.NONE)
  @ManyToMany
  @JoinTable(
      name = "community_post_industries",
      joinColumns = @JoinColumn(name = "post_id"),
      inverseJoinColumns = @JoinColumn(name = "industry_id"))
  private Set<Industry> industries = new LinkedHashSet<>();

  public CommunityPost(
      User user,
      String title,
      String content,
      String location,
      Set<Region> regions,
      Set<Industry> industries) {
    this.user = user;
    edit(title, content, location, regions, industries);
  }

  public void edit(
      String title,
      String content,
      String location,
      Set<Region> regions,
      Set<Industry> industries) {
    CommunityContentPolicy.requireText(title, 100);
    CommunityContentPolicy.requireText(content, 10000);
    CommunityContentPolicy.requireLocation(location);
    if (regions == null
        || industries == null
        || regions.size() > 2
        || industries.size() > 2
        || regions.stream().anyMatch(r -> r == null || r.getCodeType() != RegionCodeType.SIGUNGU)
        || industries.stream().anyMatch(i -> i == null || i.getStatus() != IndustryStatus.ACTIVE)) {
      throw new DomainException(ErrorCode.INVALID_COMMUNITY_TAG);
    }
    this.title = title;
    this.content = content;
    this.location = location;
    this.regions.clear();
    this.regions.addAll(regions);
    this.industries.clear();
    this.industries.addAll(industries);
  }

  public Set<Region> getRegions() {
    return Collections.unmodifiableSet(regions);
  }

  public Set<Industry> getIndustries() {
    return Collections.unmodifiableSet(industries);
  }

  public boolean isOwner(Long userId) {
    return userId != null && userId.equals(user.getId());
  }

  public void delete() {
    deletedAt = OffsetDateTime.now();
  }

  public void addLike() {
    likeCount++;
  }

  public void removeLike() {
    if (likeCount <= 0) throw new DomainException(ErrorCode.INVALID_COMMUNITY_STATE);
    likeCount--;
  }

  public void addComment() {
    commentCount++;
  }

  public void removeComment() {
    if (commentCount <= 0) throw new DomainException(ErrorCode.INVALID_COMMUNITY_STATE);
    commentCount--;
  }
}
