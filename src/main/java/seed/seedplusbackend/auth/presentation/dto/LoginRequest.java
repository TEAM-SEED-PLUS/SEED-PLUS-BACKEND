package seed.seedplusbackend.auth.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import seed.seedplusbackend.auth.application.command.LoginCommand;

@Schema(description = "로그인 요청")
public record LoginRequest(
    @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9]{4,20}$")
        @Schema(description = "영문과 숫자로 구성된 로그인 ID", example = "seedplus01")
        String loginId,
    @NotBlank @Size(min = 8, max = 72) @Schema(description = "비밀번호", example = "password123")
        String password) {

  public LoginCommand toCommand() {
    return new LoginCommand(loginId, password);
  }
}
