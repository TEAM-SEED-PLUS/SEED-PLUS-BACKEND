package seed.seedplusbackend.analysis.application.command;

import java.math.BigDecimal;

public record SurvivalAnalysisLambdaCommand(
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
    BigDecimal avgSalesAmt) {}
