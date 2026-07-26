package seed.seedplusbackend.commercial.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.commercial.domain.entity.CommercialDataCollectHistory;
import seed.seedplusbackend.commercial.domain.entity.CommercialDataCollectStatus;
import seed.seedplusbackend.commercial.domain.repository.CommercialDataCollectHistoryRepository;

public interface CommercialDataCollectHistoryJpaRepository
    extends JpaRepository<CommercialDataCollectHistory, Long>,
        CommercialDataCollectHistoryRepository {

  @Override
  java.util.Optional<CommercialDataCollectHistory> findById(Long id);

  @Override
  boolean existsByDataTypeAndTargetKeyAndStatus(
      String dataType, String targetKey, CommercialDataCollectStatus status);
}
