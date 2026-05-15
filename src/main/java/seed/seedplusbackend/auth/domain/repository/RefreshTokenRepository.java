package seed.seedplusbackend.auth.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.auth.domain.entity.RefreshToken;

public interface RefreshTokenRepository {

  <S extends RefreshToken> S save(S entity);

  Optional<RefreshToken> findById(Long id);

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  Optional<RefreshToken> findByTokenHashForUpdate(String tokenHash);

  List<RefreshToken> findAll();

  boolean existsById(Long id);

  void delete(RefreshToken entity);

  void deleteById(Long id);

  long count();
}
