package seed.seedplusbackend.builderstore.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import seed.seedplusbackend.builderstore.application.command.CreateBuilderStoreCommentCommand;

@Schema(description = "빌더스토어 댓글 작성 요청")
public record CreateBuilderStoreCommentRequest(
    @Schema(description = "부모 댓글 ID", example = "1") Long parentCommentId,
    @Schema(description = "댓글 내용", example = "좋은 창업안입니다.") @NotBlank @Size(max = 2000)
        String content) {

  public CreateBuilderStoreCommentCommand toCommand() {
    return new CreateBuilderStoreCommentCommand(parentCommentId, content);
  }
}
