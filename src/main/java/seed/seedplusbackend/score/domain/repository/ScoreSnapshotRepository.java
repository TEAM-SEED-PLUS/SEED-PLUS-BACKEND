package seed.seedplusbackend.score.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.score.domain.entity.ScoreSnapshot;
import seed.seedplusbackend.score.domain.entity.ScoreTargetType;

public interface ScoreSnapshotRepository {

  <S extends ScoreSnapshot> S save(S entity);

  Optional<ScoreSnapshot> findById(Long id);

  List<ScoreSnapshot> findAll();

  Optional<ScoreSnapshot> findFirstByStoreIdAndTargetTypeOrderByReferenceMonthDescIdDesc(
      Long storeId, ScoreTargetType targetType);

  boolean existsById(Long id);

  void delete(ScoreSnapshot entity);

  void deleteById(Long id);

  long count();
}
