package seed.seedplusbackend.builderstore.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreBookmark;

public interface BuilderStoreBookmarkRepository {

  <S extends BuilderStoreBookmark> S save(S entity);

  Optional<BuilderStoreBookmark> findById(Long id);

  Optional<BuilderStoreBookmark> findByBuilderStore_IdAndUser_Id(Long builderStoreId, Long userId);

  List<BuilderStoreBookmark> findAll();

  boolean existsByBuilderStore_IdAndUser_Id(Long builderStoreId, Long userId);

  boolean existsById(Long id);

  void delete(BuilderStoreBookmark entity);

  void deleteById(Long id);

  long count();
}
