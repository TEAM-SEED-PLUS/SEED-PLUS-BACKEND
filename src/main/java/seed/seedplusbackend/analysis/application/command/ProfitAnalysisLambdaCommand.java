package seed.seedplusbackend.analysis.application.command;

import java.math.BigDecimal;

public record ProfitAnalysisLambdaCommand(
    String industry,
    String region,
    BigDecimal area,
    BigDecimal invest,
    BigDecimal rent,
    BigDecimal premium,
    Integer staff) {}
