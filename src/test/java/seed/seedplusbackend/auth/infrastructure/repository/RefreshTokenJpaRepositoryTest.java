package seed.seedplusbackend.auth.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seed.seedplusbackend.auth.domain.entity.RefreshToken;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;
import seed.seedplusbackend.support.fixture.UserFixture;
import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.infrastructure.repository.UserJpaRepository;

@RepositoryTest
@DisplayName("RefreshTokenJpaRepository")
class RefreshTokenJpaRepositoryTest extends AbstractPostgresContainerTest {

  @Autowired private RefreshTokenJpaRepository refreshTokenJpaRepository;
  @Autowired private UserJpaRepository userJpaRepository;

  @Test
  @DisplayName("리프레시 토큰을 저장하고 조회할 수 있다")
  void saveAndFindById_smokeTest() {
    User user = userJpaRepository.save(UserFixture.generalActiveUser("rt@test.com"));

    RefreshToken saved =
        refreshTokenJpaRepository.save(
            RefreshToken.builder()
                .user(user)
                .tokenHash("hash-" + System.nanoTime())
                .expiresAt(OffsetDateTime.now().plusDays(3))
                .revokedAt(null)
                .build());

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(refreshTokenJpaRepository.findById(saved.getId())).isPresent();
    assertThat(refreshTokenJpaRepository.findByTokenHash(saved.getTokenHash())).isPresent();
  }

  @Test
  @DisplayName("findByTokenHashForUpdate는 저장된 토큰을 잠금 조회로 반환한다")
  void findByTokenHashForUpdate_returnsRow_whenExists() {
    User user = userJpaRepository.save(UserFixture.generalActiveUser("rt-lock@test.com"));
    String tokenHash = "hash-lock-" + System.nanoTime();
    RefreshToken saved =
        refreshTokenJpaRepository.save(
            RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(OffsetDateTime.now().plusDays(3))
                .revokedAt(null)
                .build());

    assertThat(refreshTokenJpaRepository.findByTokenHashForUpdate(tokenHash))
        .isPresent()
        .get()
        .extracting(RefreshToken::getId)
        .isEqualTo(saved.getId());
    assertThat(refreshTokenJpaRepository.findByTokenHashForUpdate("missing-hash")).isEmpty();
  }

  @Test
  @DisplayName("revokeByTokenHashIfNotRevoked는 미폐기 토큰만 1회 폐기한다")
  void revokeByTokenHashIfNotRevoked_updatesOnlyNotRevokedToken() {
    User user = userJpaRepository.save(UserFixture.generalActiveUser("rt-revoke"));
    String tokenHash = "hash-revoke-" + System.nanoTime();
    RefreshToken saved =
        refreshTokenJpaRepository.save(
            RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(OffsetDateTime.now().plusDays(3))
                .revokedAt(null)
                .build());
    OffsetDateTime revokedAt = OffsetDateTime.now();

    int firstUpdatedCount =
        refreshTokenJpaRepository.revokeByTokenHashIfNotRevoked(tokenHash, revokedAt);
    int secondUpdatedCount =
        refreshTokenJpaRepository.revokeByTokenHashIfNotRevoked(
            tokenHash, revokedAt.plusSeconds(1));

    assertThat(firstUpdatedCount).isEqualTo(1);
    assertThat(secondUpdatedCount).isZero();
    assertThat(refreshTokenJpaRepository.findById(saved.getId()))
        .isPresent()
        .get()
        .extracting(RefreshToken::isRevoked)
        .isEqualTo(true);
  }
}
