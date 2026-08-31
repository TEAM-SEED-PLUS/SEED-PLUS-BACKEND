package seed.seedplusbackend.commercial.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import seed.seedplusbackend.commercial.application.provider.CommercialDataType;

class SeoulRealtimeCityPopulationCollectCommandTest {

  @Test
  void trimsAreaAndUsesTenMinuteCollectionSlot() {
    SeoulRealtimeCityPopulationCollectCommand command =
        new SeoulRealtimeCityPopulationCollectCommand(" POI009 ", false);

    assertThat(command.area()).isEqualTo("POI009");
    assertThat(command.dataType()).isEqualTo(CommercialDataType.SEOUL_REALTIME_CITY_POPULATION);
    assertThat(command.targetKey()).matches("POI009:\\d{12}");
  }

  @Test
  void rejectsBlankArea() {
    assertThatThrownBy(() -> new SeoulRealtimeCityPopulationCollectCommand(" ", false))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsAreaThatCannotFitCollectionTargetKey() {
    assertThatThrownBy(() -> new SeoulRealtimeCityPopulationCollectCommand("A".repeat(81), false))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
