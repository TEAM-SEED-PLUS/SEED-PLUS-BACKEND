package seed.seedplusbackend.commercial.application.command;

import java.time.LocalDate;
import java.time.ZoneId;
import seed.seedplusbackend.commercial.application.provider.CommercialDataType;

public record SeoulSdotFootTrafficCollectCommand(boolean force)
    implements CommercialDataCollectCommand {

  private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");

  @Override
  public CommercialDataType dataType() {
    return CommercialDataType.SEOUL_SDOT_FOOT_TRAFFIC;
  }

  @Override
  public String targetKey() {
    return LocalDate.now(SEOUL_ZONE_ID).toString();
  }
}
