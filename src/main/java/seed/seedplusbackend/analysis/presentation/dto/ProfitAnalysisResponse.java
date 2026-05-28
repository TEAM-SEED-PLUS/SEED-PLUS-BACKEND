package seed.seedplusbackend.analysis.presentation.dto;

import java.math.BigDecimal;
import seed.seedplusbackend.analysis.application.result.ProfitAnalysisResult;

public record ProfitAnalysisResponse(
    ProfitInputResponse input, ProfitAssumptionsResponse assumptions, ProfitResultResponse result) {

  public static ProfitAnalysisResponse from(ProfitAnalysisResult result) {
    return new ProfitAnalysisResponse(
        ProfitInputResponse.from(result.input()),
        ProfitAssumptionsResponse.from(result.assumptions()),
        ProfitResultResponse.from(result.result()));
  }

  public record ProfitInputResponse(
      String industry,
      String region,
      BigDecimal area,
      BigDecimal invest,
      BigDecimal rent,
      BigDecimal premium,
      Integer staff) {

    private static ProfitInputResponse from(ProfitAnalysisResult.ProfitInput input) {
      return new ProfitInputResponse(
          input.industry(),
          input.region(),
          input.area(),
          input.invest(),
          input.rent(),
          input.premium(),
          input.staff());
    }
  }

  public record ProfitAssumptionsResponse(
      BigDecimal baseRevenue,
      BigDecimal regionMultiplier,
      BigDecimal baseProfitRate,
      BigDecimal variableCostRate,
      BigDecimal fixedOverheadRate,
      BigDecimal staffCostPerPerson) {

    private static ProfitAssumptionsResponse from(
        ProfitAnalysisResult.ProfitAssumptions assumptions) {
      return new ProfitAssumptionsResponse(
          assumptions.baseRevenue(),
          assumptions.regionMultiplier(),
          assumptions.baseProfitRate(),
          assumptions.variableCostRate(),
          assumptions.fixedOverheadRate(),
          assumptions.staffCostPerPerson());
    }
  }

  public record ProfitResultResponse(
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
      BigDecimal propertyScore) {

    private static ProfitResultResponse from(ProfitAnalysisResult.ProfitResult result) {
      return new ProfitResultResponse(
          result.monthlyRev(),
          result.staffCost(),
          result.fixedOverheadCost(),
          result.fixedCost(),
          result.variableCost(),
          result.staffImpact(),
          result.rentImpact(),
          result.variableImpact(),
          result.fixedOverheadImpact(),
          result.profitRate(),
          result.monthlyProfit(),
          result.totalInvest(),
          result.paybackMonths(),
          result.propertyScore());
    }
  }
}
