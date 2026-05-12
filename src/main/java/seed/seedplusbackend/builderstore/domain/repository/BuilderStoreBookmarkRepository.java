package seed.seedplusbackend.builderstore.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreBookmark;

public interface BuilderStoreBookmarkRepository {

  <S extends BuilderStoreBookmark> S save(S entity);

  Optional<BuilderStoreBookmark> findById(Long id);

  List<BuilderStoreBookmark> findAll();

  boolean existsById(Long id);

  void delete(BuilderStoreBookmark entity);

  void deleteById(Long id);

  long count();
}
