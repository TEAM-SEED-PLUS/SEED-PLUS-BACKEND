package seed.seedplusbackend.auth.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import seed.seedplusbackend.support.fixture.UserFixture;

@DisplayName("RefreshToken")
class RefreshTokenTest {

  @Test
  @DisplayName("리프레시 토큰을 폐기하면 사용할 수 없다")
  void revoke_marksTokenAsNotUsable() {
    OffsetDateTime now = OffsetDateTime.now();
    RefreshToken refreshToken =
        RefreshToken.builder()
            .user(UserFixture.generalActiveUser("01033334444", "rt-domain@test.com"))
            .tokenHash("hash")
            .expiresAt(now.plusDays(1))
            .revokedAt(null)
            .build();

    refreshToken.revoke(now);

    assertThat(refreshToken.isRevoked()).isTrue();
    assertThat(refreshToken.isUsable(now)).isFalse();
  }

  @Test
  @DisplayName("만료 시간이 지나면 리프레시 토큰은 사용할 수 없다")
  void isUsable_returnsFalse_whenExpired() {
    OffsetDateTime now = OffsetDateTime.now();
    RefreshToken refreshToken =
        RefreshToken.builder()
            .user(UserFixture.generalActiveUser("01033335555", "rt-expired@test.com"))
            .tokenHash("hash")
            .expiresAt(now.minusSeconds(1))
            .revokedAt(null)
            .build();

    assertThat(refreshToken.isExpired(now)).isTrue();
    assertThat(refreshToken.isUsable(now)).isFalse();
  }
}
