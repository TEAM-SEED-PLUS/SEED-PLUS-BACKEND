package seed.seedplusbackend.community.application.result;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import seed.seedplusbackend.community.domain.entity.CommunityPost;

public record CommunityPostResult(
    Long postId,
    CommunityAuthorResult author,
    String title,
    String content,
    String location,
    List<CommunityRegionTagResult> regionTags,
    List<CommunityIndustryTagResult> industryTags,
    long likeCount,
    long commentCount,
    boolean liked,
    boolean isOwner,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {
  public static CommunityPostResult from(CommunityPost post, Long userId, boolean liked) {
    return new CommunityPostResult(
        post.getId(),
        CommunityAuthorResult.from(post.getUser()),
        post.getTitle(),
        post.getContent(),
        post.getLocation(),
        post.getRegions().stream()
            .map(CommunityRegionTagResult::from)
            .sorted(Comparator.comparing(CommunityRegionTagResult::regionId))
            .toList(),
        post.getIndustries().stream()
            .map(CommunityIndustryTagResult::from)
            .sorted(Comparator.comparing(CommunityIndustryTagResult::industryId))
            .toList(),
        post.getLikeCount(),
        post.getCommentCount(),
        liked,
        post.isOwner(userId),
        post.getCreatedAt(),
        post.getUpdatedAt());
  }
}
