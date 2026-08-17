package seed.seedplusbackend.analysis.application.result;

import java.math.BigDecimal;
import java.util.List;

public record ProfitAnalysisResult(
    ProfitInput input,
    ProfitAssumptions assumptions,
    ProfitDynamicMetrics dynamicMetrics,
    ProfitResult result,
    List<String> dataSources,
    List<String> warnings,
    Boolean fallbackUsed) {

  public ProfitAnalysisResult(
      ProfitInput input, ProfitAssumptions assumptions, ProfitResult result) {
    this(input, assumptions, null, result, null, null, null);
  }

  public record ProfitInput(
      String storeName,
      String industry,
      String region,
      BigDecimal area,
      BigDecimal invest,
      BigDecimal rent,
      BigDecimal premium,
      Integer staff) {

    public ProfitInput(
        String industry,
        String region,
        BigDecimal area,
        BigDecimal invest,
        BigDecimal rent,
        BigDecimal premium,
        Integer staff) {
      this(null, industry, region, area, invest, rent, premium, staff);
    }
  }

  public record ProfitAssumptions(
      BigDecimal baseRevenue,
      BigDecimal regionMultiplier,
      BigDecimal baseProfitRate,
      BigDecimal variableCostRate,
      BigDecimal fixedOverheadRate,
      BigDecimal staffCostPerPerson) {}

  public record ProfitDynamicMetrics(
      BigDecimal baseRevenue,
      BigDecimal regionMultiplier,
      BigDecimal storeMarketFactor,
      BigDecimal avgSalesAmt,
      BigDecimal competitorCount,
      BigDecimal competitorDensity) {}

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
