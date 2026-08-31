package seed.seedplusbackend.analysis.infrastructure.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionRun;
import seed.seedplusbackend.analysis.domain.repository.AnalysisCollectionRunRepository;

public interface AnalysisCollectionRunJpaRepository
    extends JpaRepository<AnalysisCollectionRun, Long>, AnalysisCollectionRunRepository {

  @Override
  <S extends AnalysisCollectionRun> S save(S run);

  @Override
  @Query(
      """
      SELECT run
      FROM AnalysisCollectionRun run
      WHERE run.id = :runId
        AND run.user.id = :userId
      """)
  Optional<AnalysisCollectionRun> findByIdAndUserId(
      @Param("runId") Long runId, @Param("userId") Long userId);
}
