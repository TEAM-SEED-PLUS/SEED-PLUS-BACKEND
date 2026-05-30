package seed.seedplusbackend.builderstore.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import seed.seedplusbackend.builderstore.application.command.BuilderStoreMetricsCommand;

@Schema(description = "빌더스토어 생성 지표 요청")
public record CreateBuilderStoreMetricsRequest(
    @Schema(description = "면적", example = "40") @NotNull @Min(0) Integer area,
    @Schema(description = "예상 월 매출", example = "50000000") @NotNull @Min(0)
        Long expectedMonthlySales,
    @Schema(description = "예상 수익률", example = "12.50")
        @NotNull
        @DecimalMin("-100.00")
        @DecimalMax("1000.00")
        BigDecimal expectedProfitRate,
    @Schema(description = "투자 회수 기간(개월)", example = "36") @NotNull @Min(0)
        Integer investmentPaybackMonths,
    @Schema(description = "월 임대료", example = "2000000") @NotNull @Min(0) Long monthlyRent,
    @Schema(description = "보증금", example = "20000000") @NotNull @Min(0) Long deposit,
    @Schema(description = "투자 금액", example = "100000000") @NotNull @Min(0) Long investmentAmount) {

  public BuilderStoreMetricsCommand toCommand() {
    return new BuilderStoreMetricsCommand(
        area,
        expectedMonthlySales,
        expectedProfitRate,
        investmentPaybackMonths,
        monthlyRent,
        deposit,
        investmentAmount);
  }
}
