package seed.seedplusbackend.auth.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import seed.seedplusbackend.auth.application.command.TemporaryPasswordIssueCommand;

@Schema(description = "임시 비밀번호 발급 요청")
public record TemporaryPasswordRequest(
    @NotBlank
        @Email
        @Size(max = 320)
        @Schema(description = "가입 이메일", example = "seedplus@example.com")
        String email) {

  public TemporaryPasswordIssueCommand toCommand() {
    return new TemporaryPasswordIssueCommand(email);
  }
}
