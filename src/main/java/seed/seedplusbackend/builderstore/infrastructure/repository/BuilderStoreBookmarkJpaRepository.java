package seed.seedplusbackend.builderstore.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreBookmark;
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
        "builderStore.industry"
      })
  Optional<BuilderStoreBookmark> findByIdAndUser_Id(Long id, Long userId);

  @Override
  @EntityGraph(
      attributePaths = {
        "builderStore",
        "builderStore.region",
        "builderStore.commercialArea",
        "builderStore.industry"
      })
  Page<BuilderStoreBookmark> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  @Override
  boolean existsByBuilderStore_IdAndUser_Id(Long builderStoreId, Long userId);

  @Override
  void deleteById(Long id);
}
