package seed.seedplusbackend.commercial.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import seed.seedplusbackend.commercial.application.command.SeoulRealtimeCityPopulationCollectCommand;

public record SeoulRealtimeCityPopulationCollectRequest(
    @NotBlank @Size(max = 80) String area, boolean force) {
  public SeoulRealtimeCityPopulationCollectCommand toCommand() {
    return new SeoulRealtimeCityPopulationCollectCommand(area, force);
  }
}
