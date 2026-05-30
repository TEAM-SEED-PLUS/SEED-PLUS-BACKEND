package seed.seedplusbackend.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.domain.entity.UserRole;
import seed.seedplusbackend.user.domain.entity.UserStatus;

@Schema(description = "내 정보 응답")
public record UserMeResponse(
    @Schema(description = "사용자 이름", example = "홍길동") String name,
    @Schema(description = "사용자 역할", example = "GENERAL") UserRole role,
    @Schema(description = "사용자 상태", example = "ACTIVE") UserStatus status) {

  public static UserMeResponse from(User user) {
    return new UserMeResponse(user.getName(), user.getRole(), user.getStatus());
  }
}
