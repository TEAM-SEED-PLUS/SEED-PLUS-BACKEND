package seed.seedplusbackend.support.fixture;

import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.domain.entity.UserRole;
import seed.seedplusbackend.user.domain.entity.UserStatus;

public final class UserFixture {

  private UserFixture() {}

  public static User generalActiveUser(String email) {
    return generalActiveUser(defaultPhoneNumber(email), email);
  }

  public static User generalActiveUser(String phoneNumber, String email) {
    return User.builder()
        .phoneNumber(phoneNumber)
        .email(email)
        .password("password!@#")
        .name("일반 사용자")
        .role(UserRole.GENERAL)
        .status(UserStatus.ACTIVE)
        .build();
  }

  private static String defaultPhoneNumber(String seed) {
    return "010" + "%08d".formatted(Math.floorMod(seed.hashCode(), 100_000_000));
  }
}
