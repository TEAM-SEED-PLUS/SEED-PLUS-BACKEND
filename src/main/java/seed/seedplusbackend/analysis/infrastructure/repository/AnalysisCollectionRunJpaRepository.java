package seed.seedplusbackend.analysis.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionRun;
import seed.seedplusbackend.analysis.domain.repository.AnalysisCollectionRunRepository;

public interface AnalysisCollectionRunJpaRepository
    extends JpaRepository<AnalysisCollectionRun, Long>, AnalysisCollectionRunRepository {

  @Override
  <S extends AnalysisCollectionRun> S save(S run);
}
