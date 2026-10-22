package seed.seedplusbackend.community.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import seed.seedplusbackend.community.application.command.CreateCommunityCommentCommand;

@Schema(description = "댓글 작성 요청")
public record CreateCommunityCommentRequest(
    @Schema(description = "일반 텍스트 댓글", example = "좋은 정보 감사합니다.") @NotBlank @Size(max = 2000)
        String content) {
  public CreateCommunityCommentCommand toCommand() {
    return new CreateCommunityCommentCommand(content);
  }
}
