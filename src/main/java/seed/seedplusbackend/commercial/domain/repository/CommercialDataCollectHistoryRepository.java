package seed.seedplusbackend.commercial.domain.repository;

import java.util.Optional;
import seed.seedplusbackend.commercial.domain.entity.CommercialDataCollectHistory;
import seed.seedplusbackend.commercial.domain.entity.CommercialDataCollectStatus;

public interface CommercialDataCollectHistoryRepository {

  Optional<CommercialDataCollectHistory> findById(Long id);

  boolean existsByDataTypeAndTargetKeyAndStatus(
      String dataType, String targetKey, CommercialDataCollectStatus status);

  Optional<CommercialDataCollectHistory> findByDataTypeAndTargetKeyAndStatus(
      String dataType, String targetKey, CommercialDataCollectStatus status);

  CommercialDataCollectHistory save(CommercialDataCollectHistory history);
}
