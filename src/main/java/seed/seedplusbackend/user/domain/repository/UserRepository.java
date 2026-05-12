package seed.seedplusbackend.user.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.user.domain.entity.User;

public interface UserRepository {

  <S extends User> S save(S entity);

  Optional<User> findById(Long id);

  List<User> findAll();

  boolean existsById(Long id);

  void delete(User entity);

  void deleteById(Long id);

  long count();
}
