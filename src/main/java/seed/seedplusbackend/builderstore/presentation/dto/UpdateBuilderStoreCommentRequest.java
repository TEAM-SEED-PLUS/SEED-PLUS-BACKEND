package seed.seedplusbackend.builderstore.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import seed.seedplusbackend.builderstore.application.command.UpdateBuilderStoreCommentCommand;

@Schema(description = "빌더스토어 댓글 수정 요청")
public record UpdateBuilderStoreCommentRequest(
    @Schema(description = "댓글 내용", example = "수정된 댓글입니다.") @NotBlank @Size(max = 2000)
        String content) {

  public UpdateBuilderStoreCommentCommand toCommand() {
    return new UpdateBuilderStoreCommentCommand(content);
  }
}
