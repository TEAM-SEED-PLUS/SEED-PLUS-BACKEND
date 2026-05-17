package seed.seedplusbackend.score.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import seed.seedplusbackend.score.domain.entity.ScoreGrade;
import seed.seedplusbackend.score.domain.entity.ScoreSnapshot;
import seed.seedplusbackend.score.domain.entity.ScoreType;

@Schema(description = "점수 스냅샷 요약 응답")
public record ScoreSnapshotSummaryResponse(
    @Schema(description = "점수 유형", example = "RISK") ScoreType scoreType,
    @Schema(description = "기준 월", example = "2026-05-01") LocalDate referenceMonth,
    @Schema(description = "총점", example = "82.5") BigDecimal totalScore,
    @Schema(description = "등급", example = "A") ScoreGrade grade) {

  public static ScoreSnapshotSummaryResponse from(ScoreSnapshot scoreSnapshot) {
    return new ScoreSnapshotSummaryResponse(
        scoreSnapshot.getScoreType(),
        scoreSnapshot.getReferenceMonth(),
        scoreSnapshot.getTotalScore(),
        scoreSnapshot.getGrade());
  }
}
