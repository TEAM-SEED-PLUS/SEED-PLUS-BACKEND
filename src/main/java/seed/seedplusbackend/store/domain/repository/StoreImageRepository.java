package seed.seedplusbackend.store.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.store.domain.entity.StoreImage;

public interface StoreImageRepository {

  <S extends StoreImage> S save(S entity);

  Optional<StoreImage> findById(Long id);

  List<StoreImage> findAll();

  boolean existsById(Long id);

  void delete(StoreImage entity);

  void deleteById(Long id);

  long count();
}
