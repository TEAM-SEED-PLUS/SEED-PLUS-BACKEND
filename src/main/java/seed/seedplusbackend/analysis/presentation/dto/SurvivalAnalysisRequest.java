package seed.seedplusbackend.analysis.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import seed.seedplusbackend.analysis.application.command.SurvivalAnalysisCommand;

@Schema(description = "Survival analysis request")
public record SurvivalAnalysisRequest(
    @Schema(description = "지역 코드. regions.code 값이며 행정구역 코드를 사용합니다.", example = "1168000000")
        @NotBlank
        String regionCode,
    @Schema(description = "업종 코드", example = "I101") @NotBlank String industryCode,
    @Schema(example = "40") @NotNull @Positive BigDecimal area,
    @Schema(example = "250") @NotNull @PositiveOrZero BigDecimal rent,
    @Schema(example = "2000") @NotNull @PositiveOrZero BigDecimal deposit,
    @Schema(example = "4") @NotNull @PositiveOrZero BigDecimal avgSales,
    @Schema(example = "3") @NotNull BigDecimal salesGrowth,
    @Schema(example = "4") @NotNull @PositiveOrZero BigDecimal density,
    @Schema(example = "2") @NotNull @PositiveOrZero BigDecimal vacancy,
    @Schema(example = "4") @NotNull @PositiveOrZero BigDecimal traffic,
    @Schema(example = "2") @NotNull @PositiveOrZero BigDecimal churn,
    @Schema(example = "transfer") @NotBlank String startupType,
    @Schema(example = "4200") @NotNull @PositiveOrZero BigDecimal avgSalesAmt) {

  public SurvivalAnalysisCommand toCommand() {
    return new SurvivalAnalysisCommand(
        regionCode,
        industryCode,
        area,
        rent,
        deposit,
        avgSales,
        salesGrowth,
        density,
        vacancy,
        traffic,
        churn,
        startupType,
        avgSalesAmt);
  }
}
