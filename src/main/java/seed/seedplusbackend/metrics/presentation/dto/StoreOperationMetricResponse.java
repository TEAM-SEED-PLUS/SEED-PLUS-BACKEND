package seed.seedplusbackend.metrics.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import seed.seedplusbackend.metrics.domain.entity.StoreOperationMonthlyMetric;

@Schema(description = "점포 운영 메트릭 응답")
public record StoreOperationMetricResponse(
    @Schema(description = "기준 월", example = "2026-05-01") LocalDate referenceMonth,
    @Schema(description = "월 매출", example = "50000000") long monthlySales,
    @Schema(description = "평균 일 매출", example = "1600000") long averageDailySales,
    @Schema(description = "고객 수", example = "1200") long customerCount,
    @Schema(description = "평균 주문 금액", example = "18000") long averageOrderAmount,
    @Schema(description = "재방문율", example = "35.5") BigDecimal revisitRate,
    @Schema(description = "테이블 회전율", example = "4.2") BigDecimal tableTurnoverRate,
    @Schema(description = "배달 매출 비율", example = "25.0") BigDecimal deliverySalesRatio) {

  public static StoreOperationMetricResponse from(StoreOperationMonthlyMetric metric) {
    return new StoreOperationMetricResponse(
        metric.getReferenceMonth(),
        metric.getMonthlySales(),
        metric.getAverageDailySales(),
        metric.getCustomerCount(),
        metric.getAverageOrderAmount(),
        metric.getRevisitRate(),
        metric.getTableTurnoverRate(),
        metric.getDeliverySalesRatio());
  }

  public static List<StoreOperationMetricResponse> from(List<StoreOperationMonthlyMetric> metrics) {
    return metrics.stream().map(StoreOperationMetricResponse::from).toList();
  }
}
