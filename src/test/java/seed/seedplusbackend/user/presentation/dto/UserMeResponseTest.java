package seed.seedplusbackend.user.presentation.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.domain.entity.UserRole;
import seed.seedplusbackend.user.domain.entity.UserStatus;

class UserMeResponseTest {

  @Test
  void from_includesLoginIdAndEmailWithoutPassword() {
    User user =
        User.builder()
            .loginId("seedplus01")
            .email("seedplus@example.com")
            .phoneNumber("01012345678")
            .birthDate(LocalDate.of(1990, 1, 1))
            .password("encoded-password")
            .name("홍길동")
            .role(UserRole.GENERAL)
            .status(UserStatus.ACTIVE)
            .build();

    UserMeResponse response = UserMeResponse.from(user);

    assertThat(response.loginId()).isEqualTo("seedplus01");
    assertThat(response.email()).isEqualTo("seedplus@example.com");
    assertThat(UserMeResponse.class.getRecordComponents())
        .extracting(component -> component.getName())
        .doesNotContain("password");
  }
}
