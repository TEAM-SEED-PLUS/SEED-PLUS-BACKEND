package seed.seedplusbackend.user.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.domain.repository.UserRepository;

public interface UserJpaRepository extends JpaRepository<User, Long>, UserRepository {

  @Override
  <S extends User> S save(S entity);

  @Override
  Optional<User> findById(Long id);

  @Override
  void deleteById(Long id);
}
