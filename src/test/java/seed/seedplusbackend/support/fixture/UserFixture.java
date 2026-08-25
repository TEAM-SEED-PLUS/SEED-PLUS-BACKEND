package seed.seedplusbackend.support.fixture;

import java.time.LocalDate;
import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.domain.entity.UserRole;
import seed.seedplusbackend.user.domain.entity.UserStatus;

public final class UserFixture {

  private static final LocalDate DEFAULT_BIRTH_DATE = LocalDate.of(1990, 1, 1);

  private UserFixture() {}

  public static User generalActiveUser(String phoneNumber) {
    String resolvedPhoneNumber =
        phoneNumber.matches("^010\\d{8}$") ? phoneNumber : defaultPhoneNumber(phoneNumber);
    return generalActiveUser(resolvedPhoneNumber, DEFAULT_BIRTH_DATE);
  }

  public static User generalActiveUser(String phoneNumber, LocalDate birthDate) {
    return User.builder()
        .phoneNumber(phoneNumber)
        .loginId(defaultLoginId(phoneNumber))
        .email(defaultEmail(phoneNumber))
        .birthDate(birthDate)
        .password("password!@#")
        .name("일반 사용자")
        .role(UserRole.GENERAL)
        .status(UserStatus.ACTIVE)
        .build();
  }

  private static String defaultPhoneNumber(String seed) {
    return "010" + "%08d".formatted(Math.floorMod(seed.hashCode(), 100_000_000));
  }

  private static String defaultLoginId(String seed) {
    return "user" + Integer.toUnsignedString(seed.hashCode());
  }

  private static String defaultEmail(String seed) {
    return defaultLoginId(seed) + "@example.com";
  }
}
