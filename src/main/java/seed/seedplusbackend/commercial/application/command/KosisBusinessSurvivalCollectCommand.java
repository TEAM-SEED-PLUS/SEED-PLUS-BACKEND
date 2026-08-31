package seed.seedplusbackend.commercial.application.command;

import java.time.Year;
import java.time.ZoneId;
import seed.seedplusbackend.commercial.application.provider.CommercialDataType;

public record KosisBusinessSurvivalCollectCommand(
    Integer startYear, Integer endYear, Integer latestYearCount, boolean force)
    implements CommercialDataCollectCommand {

  private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Seoul");

  public KosisBusinessSurvivalCollectCommand {
    boolean periodSearch = startYear != null || endYear != null;
    boolean latestSearch = latestYearCount != null;

    if (periodSearch == latestSearch
        || (periodSearch && (startYear == null || endYear == null || startYear > endYear))) {
      throw new IllegalArgumentException("시작/종료 연도 또는 최근 연도 개수 중 한 가지 조회 조건만 지정해야 합니다.");
    }
  }

  @Override
  public CommercialDataType dataType() {
    return CommercialDataType.KOSIS_BUSINESS_SURVIVAL_RATE;
  }

  @Override
  public String targetKey() {
    return latestYearCount != null
        ? "LATEST:" + latestYearCount + ":" + Year.now(BUSINESS_ZONE).getValue()
        : "PERIOD:" + startYear + "-" + endYear;
  }
}
