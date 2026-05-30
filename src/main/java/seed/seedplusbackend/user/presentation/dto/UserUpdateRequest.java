package seed.seedplusbackend.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import seed.seedplusbackend.user.application.command.UpdateUserCommand;

@Schema(description = "사용자 정보 수정 요청")
public record UserUpdateRequest(
    @Pattern(regexp = ".*\\S.*", message = "공백만 입력할 수 없습니다.")
        @Size(max = 100)
        @Schema(description = "사용자 이름", example = "홍길동")
        String name,
    @Pattern(regexp = ".*\\S.*", message = "공백만 입력할 수 없습니다.")
        @Size(min = 8, max = 72)
        @Schema(description = "비밀번호. BCrypt 입력 제한을 고려해 8~72자로 검증한다.", example = "password123")
        String password) {

  public UpdateUserCommand toCommand() {
    return new UpdateUserCommand(name, password);
  }
}
