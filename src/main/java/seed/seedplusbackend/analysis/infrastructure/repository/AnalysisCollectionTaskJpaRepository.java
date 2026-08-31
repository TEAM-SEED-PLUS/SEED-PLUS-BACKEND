package seed.seedplusbackend.analysis.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionTask;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionTaskStatus;
import seed.seedplusbackend.analysis.domain.repository.AnalysisCollectionTaskRepository;

public interface AnalysisCollectionTaskJpaRepository
    extends JpaRepository<AnalysisCollectionTask, Long>, AnalysisCollectionTaskRepository {

  @Override
  <S extends AnalysisCollectionTask> S save(S task);

  @Override
  List<AnalysisCollectionTask> findAllByRunIdAndStatus(
      Long runId, AnalysisCollectionTaskStatus status);

  @Override
  Optional<AnalysisCollectionTask> findByRunIdAndDataTypeAndTargetKey(
      Long runId, String dataType, String targetKey);
}
