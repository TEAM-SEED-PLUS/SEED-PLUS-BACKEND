package seed.seedplusbackend.auth.infrastructure.repository;

import jakarta.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seed.seedplusbackend.auth.domain.entity.RefreshToken;
import seed.seedplusbackend.auth.domain.repository.RefreshTokenRepository;

public interface RefreshTokenJpaRepository
    extends JpaRepository<RefreshToken, Long>, RefreshTokenRepository {

  @Override
  <S extends RefreshToken> S save(S entity);

  @Override
  Optional<RefreshToken> findById(Long id);

  @Override
  Optional<RefreshToken> findByTokenHash(String tokenHash);

  @Override
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "select refreshToken from RefreshToken refreshToken where refreshToken.tokenHash = :tokenHash")
  Optional<RefreshToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

  @Override
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      """
      update RefreshToken refreshToken
      set refreshToken.revokedAt = :revokedAt
      where refreshToken.tokenHash = :tokenHash
        and refreshToken.revokedAt is null
      """)
  int revokeByTokenHashIfNotRevoked(
      @Param("tokenHash") String tokenHash, @Param("revokedAt") OffsetDateTime revokedAt);

  @Override
  void deleteById(Long id);
}
