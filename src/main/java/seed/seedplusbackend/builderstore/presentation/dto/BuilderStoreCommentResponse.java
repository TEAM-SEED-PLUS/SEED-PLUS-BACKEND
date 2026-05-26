package seed.seedplusbackend.builderstore.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import seed.seedplusbackend.builderstore.application.result.BuilderStoreCommentResult;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreComment;

@Schema(description = "빌더스토어 댓글 응답")
public record BuilderStoreCommentResponse(
    @Schema(description = "댓글 ID", example = "1") Long commentId,
    @Schema(description = "부모 댓글 ID", example = "1") Long parentCommentId,
    @Schema(description = "작성자 ID", example = "1") Long userId,
    @Schema(description = "작성자명", example = "일반 사용자") String userName,
    @Schema(description = "댓글 내용", example = "좋은 창업안입니다.") String content,
    @Schema(description = "작성 일시") OffsetDateTime createdAt,
    @Schema(description = "수정 일시") OffsetDateTime updatedAt,
    @Schema(description = "대댓글 목록") List<BuilderStoreCommentResponse> replies) {

  public BuilderStoreCommentResponse {
    replies = replies == null ? List.of() : List.copyOf(replies);
  }

  public static BuilderStoreCommentResponse from(BuilderStoreCommentResult result) {
    BuilderStoreComment comment = result.comment();
    return new BuilderStoreCommentResponse(
        comment.getId(),
        parentCommentId(comment),
        comment.getUser().getId(),
        comment.getUser().getName(),
        comment.getContent(),
        comment.getCreatedAt(),
        comment.getUpdatedAt(),
        result.replies().stream().map(BuilderStoreCommentResponse::fromReply).toList());
  }

  private static BuilderStoreCommentResponse fromReply(BuilderStoreComment reply) {
    return new BuilderStoreCommentResponse(
        reply.getId(),
        parentCommentId(reply),
        reply.getUser().getId(),
        reply.getUser().getName(),
        reply.getContent(),
        reply.getCreatedAt(),
        reply.getUpdatedAt(),
        List.of());
  }

  private static Long parentCommentId(BuilderStoreComment comment) {
    return comment.getParent() == null ? null : comment.getParent().getId();
  }
}
