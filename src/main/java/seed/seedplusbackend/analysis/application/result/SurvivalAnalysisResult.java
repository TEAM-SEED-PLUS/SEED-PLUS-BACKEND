package seed.seedplusbackend.analysis.application.result;

import java.math.BigDecimal;
import java.util.List;

public record SurvivalAnalysisResult(
    SurvivalInput input,
    SurvivalDerived derived,
    SurvivalDynamicMetrics dynamicMetrics,
    SurvivalScoreBreakdown scoreBreakdown,
    SurvivalResult survival,
    List<String> dataSources,
    List<String> warnings,
    Boolean fallbackUsed) {

  public SurvivalAnalysisResult(
      SurvivalInput input,
      SurvivalDerived derived,
      SurvivalScoreBreakdown scoreBreakdown,
      SurvivalResult survival) {
    this(input, derived, null, scoreBreakdown, survival, null, null, null);
  }

  public record SurvivalInput(
      String region,
      String industry,
      BigDecimal area,
      BigDecimal rent,
      BigDecimal invest,
      BigDecimal premium,
      Integer staff,
      String startupType,
      String storeName) {

    public SurvivalInput(
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
      this(region, industry, area, rent, null, deposit, null, startupType, null);
    }
  }

  public record SurvivalDerived(
      BigDecimal estMonthlyRevenue,
      BigDecimal competitionRatio,
      BigDecimal rentBurden,
      BigDecimal vitalityScore,
      BigDecimal stabilityIndex) {}

  public record SurvivalDynamicMetrics(
      BigDecimal avgSalesAmt,
      BigDecimal avgSales,
      BigDecimal salesGrowth,
      BigDecimal density,
      BigDecimal vacancy,
      BigDecimal traffic,
      BigDecimal churn,
      BigDecimal closureRate,
      BigDecimal newBusinessRate) {}

  public record SurvivalScoreBreakdown(
      BigDecimal s1_salesStability,
      BigDecimal s2_salesGrowth,
      BigDecimal s3_competition,
      BigDecimal s4_vacancyRisk,
      BigDecimal s5_traffic,
      BigDecimal s6_rentBurden,
      BigDecimal s7_churn,
      BigDecimal s8_startupTypeBonus,
      BigDecimal rawScore,
      BigDecimal totalScore) {}

  public record SurvivalResult(String grade, String survival1Year, String survival3Year) {}
}
