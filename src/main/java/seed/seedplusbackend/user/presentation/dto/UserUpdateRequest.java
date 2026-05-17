package seed.seedplusbackend.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import seed.seedplusbackend.user.application.command.UpdateUserCommand;

@Schema(description = "사용자 정보 수정 요청")
public record UserUpdateRequest(
    @Size(max = 100) @Schema(description = "사용자 이름", example = "홍길동") String name,
    @Size(min = 8, max = 72)
        @Schema(description = "비밀번호. BCrypt 입력 제한을 고려해 8~72자로 검증한다.", example = "password123")
        String password) {

  public UpdateUserCommand toCommand() {
    return new UpdateUserCommand(name, password);
  }
}
