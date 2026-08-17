package seed.seedplusbackend.commercial.application.command;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import seed.seedplusbackend.commercial.application.provider.CommercialDataType;

public record SeoulRealtimeCityPopulationCollectCommand(String area, boolean force)
    implements CommercialDataCollectCommand {

  private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");
  private static final DateTimeFormatter SLOT_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMddHHmm");

  public SeoulRealtimeCityPopulationCollectCommand {
    if (area == null || area.isBlank()) {
      throw new IllegalArgumentException("장소코드 또는 장소명은 필수입니다.");
    }
    area = area.trim();
    if (area.length() > 80) {
      throw new IllegalArgumentException("장소코드 또는 장소명은 80자 이하여야 합니다.");
    }
  }

  @Override
  public CommercialDataType dataType() {
    return CommercialDataType.SEOUL_REALTIME_CITY_POPULATION;
  }

  @Override
  public String targetKey() {
    LocalDateTime now = LocalDateTime.now(SEOUL_ZONE_ID);
    int tenMinute = now.getMinute() / 10 * 10;
    return area + ":" + now.withMinute(tenMinute).withSecond(0).withNano(0).format(SLOT_FORMATTER);
  }
}
