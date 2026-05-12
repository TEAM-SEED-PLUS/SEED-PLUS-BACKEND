package seed.seedplusbackend.builderstore.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreImage;

public interface BuilderStoreImageRepository {

  <S extends BuilderStoreImage> S save(S entity);

  Optional<BuilderStoreImage> findById(Long id);

  List<BuilderStoreImage> findAll();

  boolean existsById(Long id);

  void delete(BuilderStoreImage entity);

  void deleteById(Long id);

  long count();
}
