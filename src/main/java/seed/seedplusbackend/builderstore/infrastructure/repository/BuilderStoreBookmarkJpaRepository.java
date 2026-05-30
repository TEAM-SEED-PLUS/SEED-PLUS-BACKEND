package seed.seedplusbackend.builderstore.infrastructure.repository;

import java.util.Optional;
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
  boolean existsByBuilderStore_IdAndUser_Id(Long builderStoreId, Long userId);

  @Override
  void deleteById(Long id);
}
