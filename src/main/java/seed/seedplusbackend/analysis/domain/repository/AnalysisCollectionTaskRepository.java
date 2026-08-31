package seed.seedplusbackend.analysis.domain.repository;

import java.util.List;
import java.util.Optional;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionTask;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionTaskStatus;

public interface AnalysisCollectionTaskRepository {

  <S extends AnalysisCollectionTask> S save(S task);

  List<AnalysisCollectionTask> findAllByRunIdAndStatus(
      Long runId, AnalysisCollectionTaskStatus status);

  Optional<AnalysisCollectionTask> findByRunIdAndDataTypeAndTargetKey(
      Long runId, String dataType, String targetKey);
}
