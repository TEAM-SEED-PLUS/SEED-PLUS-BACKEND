package seed.seedplusbackend.auth.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import seed.seedplusbackend.auth.application.command.LoginCommand;

@Schema(description = "로그인 요청")
public record LoginRequest(
    @NotBlank
        @Pattern(regexp = "^010\\d{8}$")
        @Schema(description = "하이픈 없는 휴대폰 번호", example = "01012345678")
        String phoneNumber,
    @NotBlank @Size(min = 8, max = 72) @Schema(description = "비밀번호", example = "password123")
        String password) {

  public LoginCommand toCommand() {
    return new LoginCommand(phoneNumber, password);
  }
}
