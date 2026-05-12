package seed.seedplusbackend.auth.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.auth.domain.entity.RefreshToken;
import seed.seedplusbackend.auth.domain.repository.RefreshTokenRepository;

public interface RefreshTokenJpaRepository
    extends JpaRepository<RefreshToken, Long>, RefreshTokenRepository {

  @Override
  <S extends RefreshToken> S save(S entity);

  @Override
  Optional<RefreshToken> findById(Long id);

  @Override
  void deleteById(Long id);
}
