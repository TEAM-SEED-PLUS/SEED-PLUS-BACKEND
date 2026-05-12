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
  }
}
