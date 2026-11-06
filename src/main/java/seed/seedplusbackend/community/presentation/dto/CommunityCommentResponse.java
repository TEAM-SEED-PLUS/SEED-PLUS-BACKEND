package seed.seedplusbackend.community.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import seed.seedplusbackend.community.application.result.CommunityCommentResult;

@Schema(description = "댓글 응답")
public record CommunityCommentResponse(
    Long commentId,
    Long postId,
    CommunityAuthorResponse author,
    String content,
    boolean isOwner,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {
  public static CommunityCommentResponse from(CommunityCommentResult r) {
    return new CommunityCommentResponse(
        r.commentId(),
        r.postId(),
        CommunityAuthorResponse.from(r.author()),
        r.content(),
        r.isOwner(),
        r.createdAt(),
        r.updatedAt());
  }
}
