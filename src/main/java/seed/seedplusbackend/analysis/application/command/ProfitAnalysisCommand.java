package seed.seedplusbackend.analysis.application.command;

import java.math.BigDecimal;

public record ProfitAnalysisCommand(
    String industryCode,
    String regionCode,
    BigDecimal area,
    BigDecimal invest,
    BigDecimal rent,
    BigDecimal premium,
    Integer staff) {

  public ProfitAnalysisCommand {
    industryCode = normalize(industryCode);
    regionCode = normalize(regionCode);
  }

  private static String normalize(String value) {
    return value == null ? null : value.trim();
  }
}
