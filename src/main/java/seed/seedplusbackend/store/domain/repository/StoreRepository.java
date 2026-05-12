package seed.seedplusbackend.store.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.store.domain.entity.Store;

public interface StoreRepository {

  <S extends Store> S save(S entity);

  Optional<Store> findById(Long id);

  List<Store> findAll();

  boolean existsById(Long id);

  void delete(Store entity);

  void deleteById(Long id);

  long count();
}
