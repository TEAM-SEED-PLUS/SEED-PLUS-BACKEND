package seed.seedplusbackend.builderstore.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreLike;

public interface BuilderStoreLikeRepository {

  <S extends BuilderStoreLike> S save(S entity);

  Optional<BuilderStoreLike> findById(Long id);

  List<BuilderStoreLike> findAll();

  boolean existsById(Long id);

  void delete(BuilderStoreLike entity);

  void deleteById(Long id);

  long count();
}
