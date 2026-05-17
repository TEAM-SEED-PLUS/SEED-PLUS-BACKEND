package seed.seedplusbackend.metrics.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import seed.seedplusbackend.metrics.domain.entity.StoreFinancialMonthlyMetric;

@Schema(description = "점포 재무 메트릭 응답")
public record StoreFinancialMetricResponse(
    @Schema(description = "기준 월", example = "2026-05-01") LocalDate referenceMonth,
    @Schema(description = "임대료", example = "1200000") long rentAmount,
    @Schema(description = "인건비", example = "8000000") long laborCost,
    @Schema(description = "재료비", example = "15000000") long materialCost,
    @Schema(description = "고정비", example = "5000000") long fixedCost,
    @Schema(description = "변동비", example = "20000000") long variableCost,
    @Schema(description = "비용률", example = "62.5") BigDecimal costRate,
    @Schema(description = "영업이익", example = "12000000") long operatingProfit,
    @Schema(description = "영업이익률", example = "24.0") BigDecimal operatingProfitRate,
    @Schema(description = "현금흐름", example = "11000000") long cashFlow) {

  public static StoreFinancialMetricResponse from(StoreFinancialMonthlyMetric metric) {
    return new StoreFinancialMetricResponse(
        metric.getReferenceMonth(),
        metric.getRentAmount(),
        metric.getLaborCost(),
        metric.getMaterialCost(),
        metric.getFixedCost(),
        metric.getVariableCost(),
        metric.getCostRate(),
        metric.getOperatingProfit(),
        metric.getOperatingProfitRate(),
        metric.getCashFlow());
  }

  public static List<StoreFinancialMetricResponse> from(List<StoreFinancialMonthlyMetric> metrics) {
    return metrics.stream().map(StoreFinancialMetricResponse::from).toList();
  }
}
