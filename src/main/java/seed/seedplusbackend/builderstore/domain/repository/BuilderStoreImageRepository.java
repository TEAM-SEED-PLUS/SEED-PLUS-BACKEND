package seed.seedplusbackend.builderstore.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreImage;

public interface BuilderStoreImageRepository {

  <S extends BuilderStoreImage> S save(S entity);

  Optional<BuilderStoreImage> findById(Long id);

  List<BuilderStoreImage> findAll();

  List<BuilderStoreImage> findByBuilderStore_IdOrderByDisplayOrderAscIdAsc(Long builderStoreId);

  boolean existsById(Long id);

  void delete(BuilderStoreImage entity);

  void deleteByBuilderStore_Id(Long builderStoreId);

  void flush();

  void deleteById(Long id);

  long count();
}
