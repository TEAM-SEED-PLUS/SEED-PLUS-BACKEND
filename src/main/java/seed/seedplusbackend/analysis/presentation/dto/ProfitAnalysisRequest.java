package seed.seedplusbackend.analysis.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import seed.seedplusbackend.analysis.application.command.ProfitAnalysisCommand;

@Schema(description = "Profit analysis request")
public record ProfitAnalysisRequest(
    @Schema(description = "업종 코드", example = "I101") @NotBlank String industryCode,
    @Schema(description = "지역 코드. regions.code 값이며 행정구역 코드를 사용합니다.", example = "1168000000")
        @NotBlank
        String regionCode,
    @Schema(example = "30") @NotNull @Positive BigDecimal area,
    @Schema(example = "5000") @NotNull @PositiveOrZero BigDecimal invest,
    @Schema(example = "300") @NotNull @PositiveOrZero BigDecimal rent,
    @Schema(example = "2000") @NotNull @PositiveOrZero BigDecimal premium,
    @Schema(example = "3") @NotNull @PositiveOrZero Integer staff) {

  public ProfitAnalysisCommand toCommand() {
    return new ProfitAnalysisCommand(industryCode, regionCode, area, invest, rent, premium, staff);
  }
}
