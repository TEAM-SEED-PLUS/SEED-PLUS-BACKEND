package seed.seedplusbackend.user.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("User")
class UserTest {

  private static final Duration COOLDOWN = Duration.ofMinutes(1);

  @Test
  @DisplayName("임시 비밀번호를 발급하면 비밀번호와 발급 시각이 함께 설정된다")
  void issueTemporaryPassword_setsPasswordAndIssuedAt() {
    User user = activeUser();
    OffsetDateTime issuedAt = OffsetDateTime.now();

    user.issueTemporaryPassword("encoded-temporary-password", issuedAt);

    assertThat(user.getPassword()).isEqualTo("encoded-temporary-password");
    assertThat(user.getTemporaryPasswordIssuedAt()).isEqualTo(issuedAt);
    assertThat(user.isTemporaryPassword()).isTrue();
  }

  @Test
  @DisplayName("비밀번호를 변경하면 임시 비밀번호 상태가 해제된다")
  void changePassword_clearsTemporaryPasswordState() {
    User user = activeUser();
    user.issueTemporaryPassword("encoded-temporary-password", OffsetDateTime.now());

    user.changePassword("encoded-new-password");

    assertThat(user.getPassword()).isEqualTo("encoded-new-password");
    assertThat(user.isTemporaryPassword()).isFalse();
  }

  @Test
  @DisplayName("프로필 수정으로 비밀번호를 바꿔도 임시 비밀번호 상태가 해제된다")
  void updateProfile_clearsTemporaryPasswordState_whenPasswordIsGiven() {
    User user = activeUser();
    user.issueTemporaryPassword("encoded-temporary-password", OffsetDateTime.now());

    user.updateProfile("김철수", "encoded-new-password");

    assertThat(user.getName()).isEqualTo("김철수");
    assertThat(user.isTemporaryPassword()).isFalse();
  }

  @Test
  @DisplayName("프로필 수정에서 비밀번호를 빼면 임시 비밀번호 상태가 유지된다")
  void updateProfile_keepsTemporaryPasswordState_whenPasswordIsNull() {
    User user = activeUser();
    user.issueTemporaryPassword("encoded-temporary-password", OffsetDateTime.now());

    user.updateProfile("김철수", null);

    assertThat(user.getName()).isEqualTo("김철수");
    assertThat(user.isTemporaryPassword()).isTrue();
  }

  @Test
  @DisplayName("임시 비밀번호를 발급한 적이 없으면 언제든 발급할 수 있다")
  void isTemporaryPasswordReissuable_returnsTrue_whenNeverIssued() {
    User user = activeUser();

    assertThat(user.isTemporaryPasswordReissuable(OffsetDateTime.now(), COOLDOWN)).isTrue();
  }

  @Test
  @DisplayName("쿨다운이 지나지 않으면 임시 비밀번호를 다시 발급할 수 없다")
  void isTemporaryPasswordReissuable_returnsFalse_whenWithinCooldown() {
    User user = activeUser();
    OffsetDateTime issuedAt = OffsetDateTime.now();
    user.issueTemporaryPassword("encoded-temporary-password", issuedAt);

    assertThat(user.isTemporaryPasswordReissuable(issuedAt.plusSeconds(59), COOLDOWN)).isFalse();
  }

  @Test
  @DisplayName("쿨다운이 지나면 임시 비밀번호를 다시 발급할 수 있다")
  void isTemporaryPasswordReissuable_returnsTrue_whenCooldownPassed() {
    User user = activeUser();
    OffsetDateTime issuedAt = OffsetDateTime.now();
    user.issueTemporaryPassword("encoded-temporary-password", issuedAt);

    assertThat(user.isTemporaryPasswordReissuable(issuedAt.plusSeconds(60), COOLDOWN)).isTrue();
  }

  private User activeUser() {
    return User.builder()
        .phoneNumber("01012345678")
        .loginId("seedplus01")
        .email("seedplus@example.com")
        .birthDate(LocalDate.of(1990, 1, 1))
        .password("encoded-password")
        .name("홍길동")
        .role(UserRole.GENERAL)
        .status(UserStatus.ACTIVE)
        .build();
  }
}
