package seed.seedplusbackend.analysis.domain.repository;

import java.util.Optional;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionRun;

public interface AnalysisCollectionRunRepository {

  <S extends AnalysisCollectionRun> S save(S run);

  Optional<AnalysisCollectionRun> findById(Long id);
}
