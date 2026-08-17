package seed.seedplusbackend.commercial.presentation.dto;

import seed.seedplusbackend.commercial.application.command.SeoulSdotFootTrafficCollectCommand;

public record SeoulSdotFootTrafficCollectRequest(boolean force) {
  public SeoulSdotFootTrafficCollectCommand toCommand() {
    return new SeoulSdotFootTrafficCollectCommand(force);
  }
}
