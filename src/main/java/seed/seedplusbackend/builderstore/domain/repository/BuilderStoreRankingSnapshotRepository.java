package seed.seedplusbackend.builderstore.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreRankingSnapshot;

public interface BuilderStoreRankingSnapshotRepository {

  <S extends BuilderStoreRankingSnapshot> S save(S entity);

  Optional<BuilderStoreRankingSnapshot> findById(Long id);

  List<BuilderStoreRankingSnapshot> findAll();

  boolean existsById(Long id);

  void delete(BuilderStoreRankingSnapshot entity);

  void deleteById(Long id);

  long count();
}
