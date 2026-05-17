package seed.seedplusbackend.auth.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import seed.seedplusbackend.auth.application.command.SignupCommand;

@Schema(description = "회원가입 요청")
public record SignupRequest(
    @NotBlank
        @Pattern(regexp = "^010\\d{8}$")
        @Schema(description = "하이픈 없는 휴대폰 번호", example = "01012345678")
        String phoneNumber,
    @NotBlank
        @Size(min = 8, max = 72)
        @Schema(description = "비밀번호. BCrypt 입력 제한을 고려해 8~72자로 검증한다.", example = "password123")
        String password,
    @NotBlank @Size(max = 100) @Schema(description = "사용자 이름", example = "홍길동") String name,
    @NotNull
        @Past
        @Schema(description = "생년월일", example = "1990-01-01", type = "string", format = "date")
        LocalDate birthDate) {

  public SignupCommand toCommand() {
    return new SignupCommand(phoneNumber, password, name, birthDate);
  }
}
