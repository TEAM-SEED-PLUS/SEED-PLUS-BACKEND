package seed.seedplusbackend.builderstore.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStore;

public interface BuilderStoreRepository {

  <S extends BuilderStore> S save(S entity);

  Optional<BuilderStore> findById(Long id);

  List<BuilderStore> findAll();

  boolean existsById(Long id);

  void delete(BuilderStore entity);

  void deleteById(Long id);

  long count();
}
