package seed.seedplusbackend.community.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import seed.seedplusbackend.community.application.result.CommunityPostResult;

@Schema(description = "게시글 Detail 응답")
public record CommunityPostDetailResponse(
    Long postId,
    CommunityAuthorResponse author,
    String title,
    String content,
    String location,
    List<CommunityRegionTagResponse> regionTags,
    List<CommunityIndustryTagResponse> industryTags,
    long likeCount,
    long commentCount,
    boolean liked,
    boolean isOwner,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {
  public static CommunityPostDetailResponse from(CommunityPostResult r) {
    return new CommunityPostDetailResponse(
        r.postId(),
        CommunityAuthorResponse.from(r.author()),
        r.title(),
        r.content(),
        r.location(),
        r.regionTags().stream().map(CommunityRegionTagResponse::from).toList(),
        r.industryTags().stream().map(CommunityIndustryTagResponse::from).toList(),
        r.likeCount(),
        r.commentCount(),
        r.liked(),
        r.isOwner(),
        r.createdAt(),
        r.updatedAt());
  }
}
