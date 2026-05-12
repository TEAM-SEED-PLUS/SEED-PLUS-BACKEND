package seed.seedplusbackend.builderstore.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStoreRankingSnapshot;
import seed.seedplusbackend.builderstore.domain.repository.BuilderStoreRankingSnapshotRepository;

public interface BuilderStoreRankingSnapshotJpaRepository
    extends JpaRepository<BuilderStoreRankingSnapshot, Long>,
        BuilderStoreRankingSnapshotRepository {

  @Override
  <S extends BuilderStoreRankingSnapshot> S save(S entity);

  @Override
  Optional<BuilderStoreRankingSnapshot> findById(Long id);

  @Override
  void deleteById(Long id);
}
