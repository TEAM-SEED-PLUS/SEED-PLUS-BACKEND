package seed.seedplusbackend.analysis.presentation.dto;

import java.math.BigDecimal;
import seed.seedplusbackend.analysis.application.result.SurvivalAnalysisResult;

public record SurvivalAnalysisResponse(
    SurvivalInputResponse input,
    SurvivalDerivedResponse derived,
    SurvivalScoreBreakdownResponse scoreBreakdown,
    SurvivalResultResponse survival) {

  public static SurvivalAnalysisResponse from(SurvivalAnalysisResult result) {
    return new SurvivalAnalysisResponse(
        SurvivalInputResponse.from(result.input()),
        SurvivalDerivedResponse.from(result.derived()),
        SurvivalScoreBreakdownResponse.from(result.scoreBreakdown()),
        SurvivalResultResponse.from(result.survival()));
  }

  public record SurvivalInputResponse(
      String region,
      String industry,
      BigDecimal area,
      BigDecimal rent,
      BigDecimal deposit,
      BigDecimal avgSales,
      BigDecimal salesGrowth,
      BigDecimal density,
      BigDecimal vacancy,
      BigDecimal traffic,
      BigDecimal churn,
      String startupType,
      BigDecimal avgSalesAmt) {

    private static SurvivalInputResponse from(SurvivalAnalysisResult.SurvivalInput input) {
      return new SurvivalInputResponse(
          input.region(),
          input.industry(),
          input.area(),
          input.rent(),
          input.deposit(),
          input.avgSales(),
          input.salesGrowth(),
          input.density(),
          input.vacancy(),
          input.traffic(),
          input.churn(),
          input.startupType(),
          input.avgSalesAmt());
    }
  }

  public record SurvivalDerivedResponse(
      BigDecimal estMonthlyRevenue,
      BigDecimal competitionRatio,
      BigDecimal rentBurden,
      BigDecimal vitalityScore,
      BigDecimal stabilityIndex) {

    private static SurvivalDerivedResponse from(SurvivalAnalysisResult.SurvivalDerived derived) {
      return new SurvivalDerivedResponse(
          derived.estMonthlyRevenue(),
          derived.competitionRatio(),
          derived.rentBurden(),
          derived.vitalityScore(),
          derived.stabilityIndex());
    }
  }

  public record SurvivalScoreBreakdownResponse(
      BigDecimal s1_salesStability,
      BigDecimal s2_salesGrowth,
      BigDecimal s3_competition,
      BigDecimal s4_vacancyRisk,
      BigDecimal s5_traffic,
      BigDecimal s6_rentBurden,
      BigDecimal s7_churn,
      BigDecimal s8_startupTypeBonus,
      BigDecimal rawScore,
      BigDecimal totalScore) {

    private static SurvivalScoreBreakdownResponse from(
        SurvivalAnalysisResult.SurvivalScoreBreakdown scoreBreakdown) {
      return new SurvivalScoreBreakdownResponse(
          scoreBreakdown.s1_salesStability(),
          scoreBreakdown.s2_salesGrowth(),
          scoreBreakdown.s3_competition(),
          scoreBreakdown.s4_vacancyRisk(),
          scoreBreakdown.s5_traffic(),
          scoreBreakdown.s6_rentBurden(),
          scoreBreakdown.s7_churn(),
          scoreBreakdown.s8_startupTypeBonus(),
          scoreBreakdown.rawScore(),
          scoreBreakdown.totalScore());
    }
  }

  public record SurvivalResultResponse(String grade, String survival1Year, String survival3Year) {

    private static SurvivalResultResponse from(SurvivalAnalysisResult.SurvivalResult survival) {
      return new SurvivalResultResponse(
          survival.grade(), survival.survival1Year(), survival.survival3Year());
    }
  }
}
