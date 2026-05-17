package seed.seedplusbackend.score.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.score.domain.entity.ScoreSnapshot;
import seed.seedplusbackend.score.domain.entity.ScoreTargetType;
import seed.seedplusbackend.score.domain.repository.ScoreSnapshotRepository;

public interface ScoreSnapshotJpaRepository
    extends JpaRepository<ScoreSnapshot, Long>, ScoreSnapshotRepository {

  @Override
  <S extends ScoreSnapshot> S save(S entity);

  @Override
  Optional<ScoreSnapshot> findById(Long id);

  @Override
  Optional<ScoreSnapshot> findFirstByStoreIdAndTargetTypeOrderByReferenceMonthDescIdDesc(
      Long storeId, ScoreTargetType targetType);

  @Override
  void deleteById(Long id);
}
