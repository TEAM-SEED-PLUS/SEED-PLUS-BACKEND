package seed.seedplusbackend.commercial.presentation.dto;

import seed.seedplusbackend.commercial.domain.entity.CommercialAreaStatus;

public enum CommercialAreaStatusFilter {
  ACTIVE,
  INACTIVE;

  public CommercialAreaStatus toDomainStatus() {
    return CommercialAreaStatus.valueOf(name());
  }
}
