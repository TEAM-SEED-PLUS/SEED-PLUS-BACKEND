package seed.seedplusbackend.auth.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.auth.application.command.SignupCommand;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "회원가입 요청")
public class SignupRequest {

  @NotBlank
  @Pattern(regexp = "^010\\d{8}$")
  @Schema(description = "하이픈 없는 휴대폰 번호", example = "01012345678")
  private String phoneNumber;

  @NotBlank
  @Size(min = 8, max = 72)
  @Schema(description = "비밀번호. BCrypt 입력 제한을 고려해 8~72자로 검증한다.", example = "password123")
  private String password;

  @NotBlank
  @Size(max = 100)
  @Schema(description = "사용자 이름", example = "홍길동")
  private String name;

  @Email
  @Size(max = 255)
  @Schema(description = "선택 이메일", example = "user@example.com", nullable = true)
  private String email;

  public SignupCommand toCommand() {
    return new SignupCommand(phoneNumber, password, name, email);
  }
}
