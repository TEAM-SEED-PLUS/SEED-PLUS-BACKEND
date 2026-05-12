package seed.seedplusbackend.builderstore.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreComment;

public interface BuilderStoreCommentRepository {

  <S extends BuilderStoreComment> S save(S entity);

  Optional<BuilderStoreComment> findById(Long id);

  List<BuilderStoreComment> findAll();

  boolean existsById(Long id);

  void delete(BuilderStoreComment entity);

  void deleteById(Long id);

  long count();
}
