package seed.seedplusbackend.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.domain.entity.UserRole;
import seed.seedplusbackend.user.domain.entity.UserStatus;

@Getter
@Builder
@Schema(description = "내 정보 응답")
public class UserMeResponse {

  @Schema(description = "사용자 ID", example = "1")
  private final Long id;

  @Schema(description = "휴대폰 번호", example = "01012345678")
  private final String phoneNumber;

  @Schema(description = "이메일", example = "user@example.com", nullable = true)
  private final String email;

  @Schema(description = "사용자 이름", example = "홍길동")
  private final String name;

  @Schema(description = "사용자 역할", example = "GENERAL")
  private final UserRole role;

  @Schema(description = "사용자 상태", example = "ACTIVE")
  private final UserStatus status;

  public static UserMeResponse from(User user) {
    return UserMeResponse.builder()
        .id(user.getId())
        .phoneNumber(user.getPhoneNumber())
        .email(user.getEmail())
        .name(user.getName())
        .role(user.getRole())
        .status(user.getStatus())
        .build();
  }
}
