package seed.seedplusbackend.analysis.infrastructure.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionTask;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionTaskStatus;

public interface AnalysisCollectionTaskJpaRepository
    extends JpaRepository<AnalysisCollectionTask, Long> {

  List<AnalysisCollectionTask> findAllByRunIdAndStatus(
      Long runId, AnalysisCollectionTaskStatus status);
}
