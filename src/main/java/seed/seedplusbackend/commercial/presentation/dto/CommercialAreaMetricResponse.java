package seed.seedplusbackend.commercial.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import seed.seedplusbackend.commercial.application.result.CommercialAreaMetricResult;

@Schema(description = "상권 월별 메트릭 응답")
public record CommercialAreaMetricResponse(
    @Schema(description = "기준 월", example = "2026-01-01") LocalDate referenceMonth,
    @Schema(description = "유동인구", example = "120000") long floatingPopulation,
    @Schema(description = "공실률", example = "4.5") BigDecimal vacancyRate,
    @Schema(description = "평균 임대료", example = "2500000") long averageRent,
    @Schema(description = "개업률", example = "3.2") BigDecimal openingRate,
    @Schema(description = "폐업률", example = "1.7") BigDecimal closureRate,
    @Schema(description = "매출 성장률", example = "8.4") BigDecimal salesGrowthRate,
    @Schema(description = "경쟁 밀도", example = "12.3") BigDecimal competitionDensity,
    @Schema(description = "활성도 점수", example = "82") int activityScore) {

  public static CommercialAreaMetricResponse from(CommercialAreaMetricResult result) {
    return new CommercialAreaMetricResponse(
        result.referenceMonth(),
        result.floatingPopulation(),
        result.vacancyRate(),
        result.averageRent(),
        result.openingRate(),
        result.closureRate(),
        result.salesGrowthRate(),
        result.competitionDensity(),
        result.activityScore());
  }

  public static List<CommercialAreaMetricResponse> from(List<CommercialAreaMetricResult> results) {
    return results.stream().map(CommercialAreaMetricResponse::from).toList();
  }
}
