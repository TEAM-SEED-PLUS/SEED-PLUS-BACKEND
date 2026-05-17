package seed.seedplusbackend.commercial.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import seed.seedplusbackend.commercial.application.result.CommercialAreaRegionResult;
import seed.seedplusbackend.region.presentation.dto.RegionResponse;

@Schema(description = "상권 포함 지역 응답")
public record CommercialAreaRegionResponse(
    @Schema(description = "지역 정보") RegionResponse region,
    @Schema(description = "대표 지역 여부", example = "true") boolean isPrimary) {

  public static CommercialAreaRegionResponse from(CommercialAreaRegionResult result) {
    return new CommercialAreaRegionResponse(
        RegionResponse.of(
            result.regionId(),
            result.sido(),
            result.sigungu(),
            result.dong(),
            result.code(),
            result.codeType()),
        result.primary());
  }
}
