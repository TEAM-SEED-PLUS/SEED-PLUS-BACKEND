package seed.seedplusbackend.builderstore.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreBookmark;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreVisibilityStatus;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreBookmarkRepository;

public interface BuilderStoreBookmarkJpaRepository
    extends JpaRepository<BuilderStoreBookmark, Long>, BuilderStoreBookmarkRepository {

  @Override
  <S extends BuilderStoreBookmark> S save(S entity);

  @Override
  Optional<BuilderStoreBookmark> findById(Long id);

  @Override
  Optional<BuilderStoreBookmark> findByBuilderStore_IdAndUser_Id(Long builderStoreId, Long userId);

  @Override
  @EntityGraph(
      attributePaths = {
        "builderStore",
        "builderStore.region",
        "builderStore.commercialArea",
        "builderStore.industry",
        "builderStore.industry.parentIndustry"
      })
  Optional<BuilderStoreBookmark> findByIdAndUser_IdAndBuilderStore_VisibilityStatus(
      Long id, Long userId, BuilderStoreVisibilityStatus visibilityStatus);

  @Override
  @EntityGraph(
      attributePaths = {
        "builderStore",
        "builderStore.region",
        "builderStore.commercialArea",
        "builderStore.industry",
        "builderStore.industry.parentIndustry"
      })
  Page<BuilderStoreBookmark> findByUser_IdAndBuilderStore_VisibilityStatusOrderByCreatedAtDesc(
      Long userId, BuilderStoreVisibilityStatus visibilityStatus, Pageable pageable);

  @Override
  boolean existsByBuilderStore_IdAndUser_Id(Long builderStoreId, Long userId);

  @Override
  void deleteById(Long id);
}
