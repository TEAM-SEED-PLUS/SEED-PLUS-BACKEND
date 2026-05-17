package seed.seedplusbackend.commercial.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import seed.seedplusbackend.commercial.application.result.CommercialAreaDetailResult;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaType;

@Schema(description = "상권 상세 응답")
public record CommercialAreaDetailResponse(
    @Schema(description = "상권 ID", example = "1") Long commercialAreaId,
    @Schema(description = "상권명", example = "강남역") String name,
    @Schema(description = "상권 유형", example = "DEVELOPED") CommercialAreaType type,
    @Schema(description = "상권 상태", example = "ACTIVE") String status,
    @Schema(description = "상권 설명", example = "강남역 일대 주요 상권") String description,
    @Schema(description = "상권 포함 지역 목록") List<CommercialAreaRegionResponse> regions,
    @Schema(description = "최신 상권 메트릭") CommercialAreaMetricResponse latestMetric) {

  public CommercialAreaDetailResponse {
    regions = regions == null ? List.of() : List.copyOf(regions);
  }

  public static CommercialAreaDetailResponse from(CommercialAreaDetailResult result) {
    return new CommercialAreaDetailResponse(
        result.commercialAreaId(),
        result.name(),
        result.type(),
        result.status().name(),
        result.description(),
        result.regions().stream().map(CommercialAreaRegionResponse::from).toList(),
        result.latestMetric() == null
            ? null
            : CommercialAreaMetricResponse.from(result.latestMetric()));
  }
}
