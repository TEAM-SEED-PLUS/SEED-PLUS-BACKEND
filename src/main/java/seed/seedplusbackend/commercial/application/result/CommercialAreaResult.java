package seed.seedplusbackend.commercial.application.result;

import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaStatus;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaType;

public record CommercialAreaResult(
    Long commercialAreaId, String name, CommercialAreaType type, CommercialAreaStatus status) {

  public static CommercialAreaResult from(CommercialArea commercialArea) {
    return new CommercialAreaResult(
        commercialArea.getId(),
        commercialArea.getName(),
        commercialArea.getType(),
        commercialArea.getStatus());
  }
}
