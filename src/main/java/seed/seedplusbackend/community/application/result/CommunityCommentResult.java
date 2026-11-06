package seed.seedplusbackend.community.application.result;

import java.time.OffsetDateTime;
import seed.seedplusbackend.community.domain.entity.CommunityComment;

public record CommunityCommentResult(
    Long commentId,
    Long postId,
    CommunityAuthorResult author,
    String content,
    boolean isOwner,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {
  public static CommunityCommentResult from(CommunityComment comment, Long userId) {
    return new CommunityCommentResult(
        comment.getId(),
        comment.getPost().getId(),
        CommunityAuthorResult.from(comment.getUser()),
        comment.getContent(),
        comment.isOwner(userId),
        comment.getCreatedAt(),
        comment.getUpdatedAt());
  }
}
