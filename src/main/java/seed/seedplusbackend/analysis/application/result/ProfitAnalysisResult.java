package seed.seedplusbackend.analysis.application.result;

import java.math.BigDecimal;

public record ProfitAnalysisResult(
    ProfitInput input, ProfitAssumptions assumptions, ProfitResult result) {

  public record ProfitInput(
      String industry,
      String region,
      BigDecimal area,
      BigDecimal invest,
      BigDecimal rent,
      BigDecimal premium,
      Integer staff) {}

  public record ProfitAssumptions(
      BigDecimal baseRevenue,
      BigDecimal regionMultiplier,
      BigDecimal baseProfitRate,
      BigDecimal variableCostRate,
      BigDecimal fixedOverheadRate,
      BigDecimal staffCostPerPerson) {}

  public record ProfitResult(
      BigDecimal monthlyRev,
      BigDecimal staffCost,
      BigDecimal fixedOverheadCost,
      BigDecimal fixedCost,
      BigDecimal variableCost,
      BigDecimal staffImpact,
      BigDecimal rentImpact,
      BigDecimal variableImpact,
      BigDecimal fixedOverheadImpact,
      BigDecimal profitRate,
      BigDecimal monthlyProfit,
      BigDecimal totalInvest,
      BigDecimal paybackMonths,
      BigDecimal propertyScore) {}
}
