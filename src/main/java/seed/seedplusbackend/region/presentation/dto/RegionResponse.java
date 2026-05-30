package seed.seedplusbackend.region.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.domain.entity.RegionCodeType;

@Schema(description = "지역 응답")
public record RegionResponse(
    @Schema(description = "지역 ID", example = "1") Long regionId,
    @Schema(description = "시도명", example = "서울특별시") String sido,
    @Schema(description = "시군구명", example = "강남구") String sigungu,
    @Schema(description = "동명", example = "역삼동") String dong,
    @Schema(description = "지역 코드", example = "1168010100") String code,
    @Schema(description = "코드 유형", example = "LEGAL_DONG") RegionCodeType codeType) {

  public static RegionResponse from(Region region) {
    return new RegionResponse(
        region.getId(),
        region.getSido(),
        region.getSigungu(),
        region.getDong(),
        region.getCode(),
        region.getCodeType());
  }

  public static RegionResponse of(
      Long regionId,
      String sido,
      String sigungu,
      String dong,
      String code,
      RegionCodeType codeType) {
    return new RegionResponse(regionId, sido, sigungu, dong, code, codeType);
  }
}
