package seed.seedplusbackend.score.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.score.domain.entity.ScoreSnapshot;
import seed.seedplusbackend.score.domain.entity.ScoreTargetType;
import seed.seedplusbackend.score.domain.entity.ScoreType;

public interface ScoreSnapshotRepository {

  <S extends ScoreSnapshot> S save(S entity);

  Optional<ScoreSnapshot> findById(Long id);

  List<ScoreSnapshot> findAll();

  Optional<ScoreSnapshot> findFirstByStoreIdAndTargetTypeOrderByReferenceMonthDescIdDesc(
      Long storeId, ScoreTargetType targetType);

  Optional<ScoreSnapshot> findFirstByBuilderStoreIdAndTargetTypeOrderByReferenceMonthDescIdDesc(
      Long builderStoreId, ScoreTargetType targetType);

  Optional<ScoreSnapshot>
      findFirstByBuilderStoreIdAndTargetTypeAndScoreTypeOrderByReferenceMonthDescIdDesc(
          Long builderStoreId, ScoreTargetType targetType, ScoreType scoreType);

  boolean existsByBuilderStoreIdAndTargetTypeAndScoreTypeAndReferenceMonth(
      Long builderStoreId,
      ScoreTargetType targetType,
      ScoreType scoreType,
      LocalDate referenceMonth);

  boolean existsById(Long id);

  void delete(ScoreSnapshot entity);

  void deleteById(Long id);

  long count();
}
