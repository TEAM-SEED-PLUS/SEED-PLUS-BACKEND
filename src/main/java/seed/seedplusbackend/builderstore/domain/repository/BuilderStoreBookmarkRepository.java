package seed.seedplusbackend.builderstore.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreBookmark;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreVisibilityStatus;

public interface BuilderStoreBookmarkRepository {

  <S extends BuilderStoreBookmark> S save(S entity);

  Optional<BuilderStoreBookmark> findById(Long id);

  Optional<BuilderStoreBookmark> findByBuilderStore_IdAndUser_Id(Long builderStoreId, Long userId);

  Optional<BuilderStoreBookmark> findByIdAndUser_IdAndBuilderStore_VisibilityStatus(
      Long id, Long userId, BuilderStoreVisibilityStatus visibilityStatus);

  Page<BuilderStoreBookmark> findByUser_IdAndBuilderStore_VisibilityStatusOrderByCreatedAtDesc(
      Long userId, BuilderStoreVisibilityStatus visibilityStatus, Pageable pageable);

  List<BuilderStoreBookmark> findAll();

  boolean existsByBuilderStore_IdAndUser_Id(Long builderStoreId, Long userId);

  boolean existsById(Long id);

  void delete(BuilderStoreBookmark entity);

  void deleteById(Long id);

  long count();
}
