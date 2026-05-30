package seed.seedplusbackend.score.infrastructure.repository;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.score.domain.entity.ScoreSnapshot;
import seed.seedplusbackend.score.domain.entity.ScoreTargetType;
import seed.seedplusbackend.score.domain.entity.ScoreType;
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
  Optional<ScoreSnapshot> findFirstByBuilderStoreIdAndTargetTypeOrderByReferenceMonthDescIdDesc(
      Long builderStoreId, ScoreTargetType targetType);

  @Override
  Optional<ScoreSnapshot>
      findFirstByBuilderStoreIdAndTargetTypeAndScoreTypeOrderByReferenceMonthDescIdDesc(
          Long builderStoreId, ScoreTargetType targetType, ScoreType scoreType);

  @Override
  boolean existsByBuilderStoreIdAndTargetTypeAndScoreTypeAndReferenceMonth(
      Long builderStoreId,
      ScoreTargetType targetType,
      ScoreType scoreType,
      LocalDate referenceMonth);

  @Override
  void deleteById(Long id);
}
