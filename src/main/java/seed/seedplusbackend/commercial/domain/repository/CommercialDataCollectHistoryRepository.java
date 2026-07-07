package seed.seedplusbackend.commercial.domain.repository;

import seed.seedplusbackend.commercial.domain.entity.CommercialDataCollectHistory;
import seed.seedplusbackend.commercial.domain.entity.CommercialDataCollectStatus;

public interface CommercialDataCollectHistoryRepository {

  boolean existsByDataTypeAndTargetKeyAndStatus(
      String dataType, String targetKey, CommercialDataCollectStatus status);

  CommercialDataCollectHistory save(CommercialDataCollectHistory history);
}
