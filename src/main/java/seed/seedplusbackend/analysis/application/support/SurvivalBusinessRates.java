package seed.seedplusbackend.analysis.application.support;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import seed.seedplusbackend.analysis.application.command.SurvivalAnalysisLambdaCommand;
import seed.seedplusbackend.analysis.application.result.SurvivalAnalysisResult;
import seed.seedplusbackend.analysis.application.result.SurvivalAnalysisResult.SurvivalDynamicMetrics;

/** 최신 연도에서 제공된 기업수만 사용하고 미제공 비율을 실제 0과 구분한다. */
public final class SurvivalBusinessRates {
  private SurvivalBusinessRates() {}

  public static SurvivalAnalysisResult normalize(
      SurvivalAnalysisResult result, SurvivalAnalysisLambdaCommand command) {
    if (result == null || result.dynamicMetrics() == null) return result;
    BigDecimal closure = rate(command.closedBusinesses(), command.activeBusinesses());
    BigDecimal births = rate(command.newBusinesses(), command.activeBusinesses());
    var metrics = result.dynamicMetrics();
    var warnings = new ArrayList<String>();
    if (result.warnings() != null) warnings.addAll(result.warnings());
    if (closure == null) warnings.add("KOSIS 최신 연도 소멸·활동 기업수 부족 — 폐업률 미제공");
    if (births == null) warnings.add("KOSIS 최신 연도 신생·활동 기업수 부족 — 신생률 미제공");
    return new SurvivalAnalysisResult(
        result.input(),
        result.derived(),
        new SurvivalDynamicMetrics(
            metrics.avgSalesAmt(),
            metrics.avgSales(),
            metrics.salesGrowth(),
            metrics.density(),
            metrics.vacancy(),
            metrics.traffic(),
            metrics.churn(),
            closure,
            births),
        result.scoreBreakdown(),
        result.survival(),
        result.dataSources(),
        warnings,
        Boolean.TRUE.equals(result.fallbackUsed()) || closure == null || births == null);
  }

  private static BigDecimal rate(BigDecimal count, BigDecimal active) {
    if (count == null || active == null || active.signum() <= 0) return null;
    return count.multiply(BigDecimal.valueOf(100)).divide(active, 2, RoundingMode.HALF_UP);
  }
}
