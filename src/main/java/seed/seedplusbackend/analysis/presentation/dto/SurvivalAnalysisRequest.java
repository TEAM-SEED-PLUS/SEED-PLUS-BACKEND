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
    @Schema(description = "상가명", example = "강남스타카페") @NotBlank String storeName,
    @Schema(description = "지역 코드. regions.code 값이며 행정구역 코드를 사용합니다.", example = "1168000000")
        @NotBlank
        String regionCode,
    @Schema(description = "업종 코드", example = "I101") @NotBlank String industryCode,
    @Schema(example = "40") @NotNull @Positive BigDecimal area,
    @Schema(example = "250") @NotNull @PositiveOrZero BigDecimal rent,
    @Schema(example = "8000") @NotNull @PositiveOrZero BigDecimal invest,
    @Schema(example = "2000") @NotNull @PositiveOrZero BigDecimal premium,
    @Schema(example = "3") @NotNull @PositiveOrZero Integer staff,
    @Schema(description = "수집 실패 후 재시도할 실행 ID. 최초 요청에서는 생략합니다.", example = "7") @Positive
        Long collectionRunId) {

  public SurvivalAnalysisCommand toCommand() {
    return new SurvivalAnalysisCommand(
        storeName, industryCode, regionCode, area, invest, rent, premium, staff, collectionRunId);
  }
}
