package seed.seedplusbackend.commercial.application.command;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import seed.seedplusbackend.commercial.application.provider.CommercialDataType;

class SeoulSdotFootTrafficCollectCommandTest {

  @Test
  void usesSeoulCollectionDateAsTargetKey() {
    SeoulSdotFootTrafficCollectCommand command = new SeoulSdotFootTrafficCollectCommand(false);

    assertThat(command.dataType()).isEqualTo(CommercialDataType.SEOUL_SDOT_FOOT_TRAFFIC);
    assertThat(command.targetKey()).isEqualTo(LocalDate.now(ZoneId.of("Asia/Seoul")).toString());
  }
}
