package seed.seedplusbackend.commercial.application.query;

import seed.seedplusbackend.commercial.domain.entity.CommercialAreaStatus;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaType;

public record CommercialAreaSearchQuery(
    int page, int size, Long regionId, CommercialAreaType type, CommercialAreaStatus status) {

  public CommercialAreaSearchQuery {
    status = status == null ? CommercialAreaStatus.ACTIVE : status;
  }
}
