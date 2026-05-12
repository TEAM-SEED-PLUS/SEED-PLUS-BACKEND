package seed.seedplusbackend.score.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.score.domain.entity.ScoreComponentSnapshot;
import seed.seedplusbackend.score.domain.repository.ScoreComponentSnapshotRepository;

public interface ScoreComponentSnapshotJpaRepository
    extends JpaRepository<ScoreComponentSnapshot, Long>, ScoreComponentSnapshotRepository {

  @Override
  <S extends ScoreComponentSnapshot> S save(S entity);

  @Override
  Optional<ScoreComponentSnapshot> findById(Long id);

  @Override
  void deleteById(Long id);
}
