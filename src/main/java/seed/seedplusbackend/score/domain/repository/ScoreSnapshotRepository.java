package seed.seedplusbackend.score.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.score.domain.entity.ScoreSnapshot;

public interface ScoreSnapshotRepository {

  <S extends ScoreSnapshot> S save(S entity);

  Optional<ScoreSnapshot> findById(Long id);

  List<ScoreSnapshot> findAll();

  boolean existsById(Long id);

  void delete(ScoreSnapshot entity);

  void deleteById(Long id);

  long count();
}
