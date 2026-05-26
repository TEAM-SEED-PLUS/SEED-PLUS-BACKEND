package seed.seedplusbackend.builderstore.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreComment;

public interface BuilderStoreCommentRepository {

  <S extends BuilderStoreComment> S save(S entity);

  Optional<BuilderStoreComment> findById(Long id);

  Optional<BuilderStoreComment> findByIdAndBuilderStore_Id(Long id, Long builderStoreId);

  List<BuilderStoreComment> findAll();

  Page<BuilderStoreComment> findByBuilderStore_IdOrderByCreatedAtAscIdAsc(
      Long builderStoreId, Pageable pageable);

  Page<BuilderStoreComment> findByBuilderStore_IdAndParentIsNullOrderByCreatedAtAscIdAsc(
      Long builderStoreId, Pageable pageable);

  List<BuilderStoreComment> findByParent_IdOrderByCreatedAtAscIdAsc(Long parentId);

  boolean existsById(Long id);

  void delete(BuilderStoreComment entity);

  void deleteById(Long id);

  long count();
}
