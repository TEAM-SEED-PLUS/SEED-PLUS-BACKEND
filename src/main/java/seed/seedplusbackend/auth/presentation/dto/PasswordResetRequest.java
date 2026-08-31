package seed.seedplusbackend.auth.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import seed.seedplusbackend.auth.application.command.PasswordResetCommand;

@Schema(description = "비밀번호 재설정 요청")
public record PasswordResetRequest(
    @NotBlank
        @Email
        @Size(max = 320)
        @Schema(description = "가입 이메일", example = "seedplus@example.com")
        String email,
    @NotBlank @Size(min = 8, max = 72) @Schema(description = "기존 비밀번호", example = "password123")
        String currentPassword,
    @NotBlank @Size(min = 8, max = 72) @Schema(description = "새 비밀번호", example = "newpassword123")
        String newPassword,
    @NotBlank
        @Size(min = 8, max = 72)
        @Schema(description = "새 비밀번호 확인", example = "newpassword123")
        String newPasswordConfirmation) {

  public PasswordResetCommand toCommand() {
    return new PasswordResetCommand(email, currentPassword, newPassword, newPasswordConfirmation);
  }
}
