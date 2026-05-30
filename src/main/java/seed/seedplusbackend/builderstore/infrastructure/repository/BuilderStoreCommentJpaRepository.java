package seed.seedplusbackend.builderstore.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreComment;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreCommentRepository;

public interface BuilderStoreCommentJpaRepository
    extends JpaRepository<BuilderStoreComment, Long>, BuilderStoreCommentRepository {

  @Override
  <S extends BuilderStoreComment> S save(S entity);

  @Override
  Optional<BuilderStoreComment> findById(Long id);

  @Override
  @EntityGraph(attributePaths = {"user", "parent"})
  Optional<BuilderStoreComment> findByIdAndBuilderStore_Id(Long id, Long builderStoreId);

  @Override
  @EntityGraph(attributePaths = {"user", "parent"})
  Page<BuilderStoreComment> findByBuilderStore_IdOrderByCreatedAtAscIdAsc(
      Long builderStoreId, Pageable pageable);

  @Override
  @EntityGraph(attributePaths = {"user", "parent"})
  Page<BuilderStoreComment> findByBuilderStore_IdAndParentIsNullOrderByCreatedAtAscIdAsc(
      Long builderStoreId, Pageable pageable);

  @Override
  @EntityGraph(attributePaths = {"user", "parent"})
  java.util.List<BuilderStoreComment> findByParent_IdOrderByCreatedAtAscIdAsc(Long parentId);

  @Override
  void deleteById(Long id);
}
