package seed.seedplusbackend.community.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import seed.seedplusbackend.community.application.result.CommunityPostResult;

@Schema(description = "게시글 Summary 응답")
public record CommunityPostSummaryResponse(
    Long postId,
    CommunityAuthorResponse author,
    String title,
    String contentSummary,
    String location,
    List<CommunityRegionTagResponse> regionTags,
    List<CommunityIndustryTagResponse> industryTags,
    long likeCount,
    long commentCount,
    boolean liked,
    boolean isOwner,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {
  public static CommunityPostSummaryResponse from(CommunityPostResult r) {
    return new CommunityPostSummaryResponse(
        r.postId(),
        CommunityAuthorResponse.from(r.author()),
        r.title(),
        r.content().substring(0, Math.min(200, r.content().length())),
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
