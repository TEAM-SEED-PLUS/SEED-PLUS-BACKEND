package seed.seedplusbackend.score.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.score.domain.entity.ScoreComponentSnapshot;

public interface ScoreComponentSnapshotRepository {

  <S extends ScoreComponentSnapshot> S save(S entity);

  Optional<ScoreComponentSnapshot> findById(Long id);

  List<ScoreComponentSnapshot> findAll();

  boolean existsById(Long id);

  void delete(ScoreComponentSnapshot entity);

  void deleteById(Long id);

  long count();
}
