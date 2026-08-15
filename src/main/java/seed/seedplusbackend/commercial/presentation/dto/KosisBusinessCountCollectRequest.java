package seed.seedplusbackend.commercial.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import seed.seedplusbackend.commercial.application.command.KosisBusinessCountCollectCommand;

public record KosisBusinessCountCollectRequest(
    @Schema(description = "조회 시작 연도. latestYearCount와 함께 사용할 수 없습니다.", example = "2021")
        @Min(1900)
        @Max(2200)
        Integer startYear,
    @Schema(description = "조회 종료 연도. latestYearCount와 함께 사용할 수 없습니다.", example = "2023")
        @Min(1900)
        @Max(2200)
        Integer endYear,
    @Schema(description = "조회할 최신 연도 개수. 시작·종료 연도와 함께 사용할 수 없습니다.", example = "3") @Min(1) @Max(100)
        Integer latestYearCount,
    @Schema(description = "이미 완료된 동일 조건을 강제로 다시 수집할지 여부", example = "false") boolean force) {

  @AssertTrue(message = "시작/종료 연도 또는 최근 연도 개수 중 한 가지 조회 조건만 지정해야 합니다.")
  public boolean isValidSearchCondition() {
    boolean periodSearch = startYear != null || endYear != null;
    boolean latestSearch = latestYearCount != null;
    return periodSearch != latestSearch
        && (!periodSearch || (startYear != null && endYear != null && startYear <= endYear));
  }

  public KosisBusinessCountCollectCommand toCommand() {
    return new KosisBusinessCountCollectCommand(startYear, endYear, latestYearCount, force);
  }
}
