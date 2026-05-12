package seed.seedplusbackend.store.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.store.domain.entity.Store;
import seed.seedplusbackend.store.domain.repository.StoreRepository;

public interface StoreJpaRepository extends JpaRepository<Store, Long>, StoreRepository {

  @Override
  <S extends Store> S save(S entity);

  @Override
  Optional<Store> findById(Long id);

  @Override
  void deleteById(Long id);
}
