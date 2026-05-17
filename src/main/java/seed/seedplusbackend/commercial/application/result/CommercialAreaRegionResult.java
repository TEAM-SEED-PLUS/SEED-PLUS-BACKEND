package seed.seedplusbackend.commercial.application.result;

import seed.seedplusbackend.commercial.domain.entity.CommercialAreaRegionMapping;
import seed.seedplusbackend.region.domain.entity.RegionCodeType;

public record CommercialAreaRegionResult(
    Long regionId,
    String sido,
    String sigungu,
    String dong,
    String code,
    RegionCodeType codeType,
    boolean primary) {

  public static CommercialAreaRegionResult from(CommercialAreaRegionMapping mapping) {
    return new CommercialAreaRegionResult(
        mapping.getRegion().getId(),
        mapping.getRegion().getSido(),
        mapping.getRegion().getSigungu(),
        mapping.getRegion().getDong(),
        mapping.getRegion().getCode(),
        mapping.getRegion().getCodeType(),
        mapping.isPrimary());
  }
}
