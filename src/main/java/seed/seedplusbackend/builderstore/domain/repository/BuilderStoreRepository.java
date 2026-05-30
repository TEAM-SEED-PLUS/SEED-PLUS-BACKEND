package seed.seedplusbackend.builderstore.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStore;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreVisibilityStatus;

public interface BuilderStoreRepository {

  <S extends BuilderStore> S save(S entity);

  Optional<BuilderStore> findById(Long id);

  Optional<BuilderStore> findByIdAndVisibilityStatus(Long id, BuilderStoreVisibilityStatus status);

  Optional<BuilderStore> findByIdAndVisibilityStatusNot(
      Long id, BuilderStoreVisibilityStatus status);

  List<BuilderStore> findAll();

  Page<BuilderStore> searchPublic(
      Long regionId,
      Long commercialAreaId,
      Long industryId,
      Integer minArea,
      Integer maxArea,
      Pageable pageable);

  Page<BuilderStore> findByUser_IdAndVisibilityStatusNot(
      Long userId, BuilderStoreVisibilityStatus status, Pageable pageable);

  boolean existsById(Long id);

  void delete(BuilderStore entity);

  void deleteById(Long id);

  long count();
}
