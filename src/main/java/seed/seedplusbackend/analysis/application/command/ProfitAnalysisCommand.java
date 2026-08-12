package seed.seedplusbackend.analysis.application.command;

import java.math.BigDecimal;

public record ProfitAnalysisCommand(
    String storeName,
    String industryCode,
    String regionCode,
    BigDecimal area,
    BigDecimal invest,
    BigDecimal rent,
    BigDecimal premium,
    Integer staff) {

  public ProfitAnalysisCommand {
    storeName = normalize(storeName);
    industryCode = normalize(industryCode);
    regionCode = normalize(regionCode);
  }

  private static String normalize(String value) {
    return value == null ? null : value.trim();
  }
}
