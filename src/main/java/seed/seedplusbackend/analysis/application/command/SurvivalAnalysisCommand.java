package seed.seedplusbackend.analysis.application.command;

import java.math.BigDecimal;

public record SurvivalAnalysisCommand(
    String regionCode,
    String industryCode,
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

  public SurvivalAnalysisCommand {
    regionCode = normalize(regionCode);
    industryCode = normalize(industryCode);
    startupType = normalize(startupType);
  }

  private static String normalize(String value) {
    return value == null ? null : value.trim();
  }
}
