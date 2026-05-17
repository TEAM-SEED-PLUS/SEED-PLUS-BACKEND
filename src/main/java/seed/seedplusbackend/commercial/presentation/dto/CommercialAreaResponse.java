package seed.seedplusbackend.commercial.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import seed.seedplusbackend.commercial.application.result.CommercialAreaResult;
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaType;

@Schema(description = "상권 응답")
public record CommercialAreaResponse(
    @Schema(description = "상권 ID", example = "1") Long commercialAreaId,
    @Schema(description = "상권명", example = "강남역") String name,
    @Schema(description = "상권 유형", example = "DEVELOPED") CommercialAreaType type,
    @Schema(description = "상권 상태", example = "ACTIVE") String status) {

  public static CommercialAreaResponse from(CommercialAreaResult result) {
    return new CommercialAreaResponse(
        result.commercialAreaId(), result.name(), result.type(), result.status().name());
  }

  public static CommercialAreaResponse from(CommercialArea commercialArea) {
    return new CommercialAreaResponse(
        commercialArea.getId(),
        commercialArea.getName(),
        commercialArea.getType(),
        commercialArea.getStatus().name());
  }

  public static List<CommercialAreaResponse> from(List<CommercialAreaResult> results) {
    return results.stream().map(CommercialAreaResponse::from).toList();
  }
}
