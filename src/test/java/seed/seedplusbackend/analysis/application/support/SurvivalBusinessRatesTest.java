package seed.seedplusbackend.analysis.application.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import seed.seedplusbackend.analysis.application.command.SurvivalAnalysisLambdaCommand;
import seed.seedplusbackend.analysis.application.result.SurvivalAnalysisResult;
import seed.seedplusbackend.analysis.application.result.SurvivalAnalysisResult.SurvivalDynamicMetrics;

class SurvivalBusinessRatesTest {
  @Test
  @DisplayName("소멸 기업수가 없어도 신생률은 계산하고 폐업률만 null로 반환한다")
  void missingClosedCountPreservesBirthRate() {
    var result = normalize(null, new BigDecimal("916804"), new BigDecimal("143842"));
    assertThat(result.dynamicMetrics().closureRate()).isNull();
    assertThat(result.dynamicMetrics().newBusinessRate()).isEqualByComparingTo("15.69");
    assertThat(result.dynamicMetrics().churn()).isEqualByComparingTo("3");
    assertThat(result.fallbackUsed()).isTrue();
    assertThat(result.warnings()).contains("기존 경고");
    assertThat(result.warnings()).anyMatch(w -> w.contains("폐업률 미제공"));
  }

  @Test
  @DisplayName("실제 기업수가 0이면 누락이 아닌 비율 0으로 반환한다")
  void genuineZeroIsNotMissing() {
    var result = normalize(BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.ZERO);
    assertThat(result.dynamicMetrics().closureRate()).isEqualByComparingTo("0");
    assertThat(result.dynamicMetrics().newBusinessRate()).isEqualByComparingTo("0");
    assertThat(result.fallbackUsed()).isFalse();
  }

  @Test
  @DisplayName("활동 기업수가 없거나 0이면 두 비율 모두 미제공 처리한다")
  void missingOrZeroDenominator() {
    for (BigDecimal active : new BigDecimal[] {null, BigDecimal.ZERO}) {
      var result = normalize(BigDecimal.ONE, active, BigDecimal.ONE);
      assertThat(result.dynamicMetrics().closureRate()).isNull();
      assertThat(result.dynamicMetrics().newBusinessRate()).isNull();
    }
  }

  private SurvivalAnalysisResult normalize(
      BigDecimal closed, BigDecimal active, BigDecimal births) {
    var command =
        new SurvivalAnalysisLambdaCommand(
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, closed, active, births, false, List.of());
    var result =
        new SurvivalAnalysisResult(
            null,
            null,
            new SurvivalDynamicMetrics(
                null,
                null,
                null,
                null,
                null,
                null,
                BigDecimal.valueOf(3),
                BigDecimal.ZERO,
                BigDecimal.ZERO),
            null,
            null,
            List.of(),
            List.of("기존 경고"),
            false);
    return SurvivalBusinessRates.normalize(result, command);
  }
}
