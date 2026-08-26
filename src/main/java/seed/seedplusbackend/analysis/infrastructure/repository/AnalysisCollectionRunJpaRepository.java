package seed.seedplusbackend.analysis.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionRun;

public interface AnalysisCollectionRunJpaRepository
    extends JpaRepository<AnalysisCollectionRun, Long> {}
