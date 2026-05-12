package seed.seedplusbackend.support.fixture;

import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.domain.entity.UserRole;
import seed.seedplusbackend.user.domain.entity.UserStatus;

public final class UserFixture {

  private UserFixture() {}

  public static User generalActiveUser(String email) {
    return User.builder()
        .email(email)
        .password("password!@#")
        .name("일반 사용자")
        .role(UserRole.GENERAL)
        .status(UserStatus.ACTIVE)
        .build();
  }
}
