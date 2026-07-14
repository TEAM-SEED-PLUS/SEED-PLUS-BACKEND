package seed.seedplusbackend.commercial.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import seed.seedplusbackend.commercial.application.command.SmallBusinessStoreCollectCommand;

public record SmallBusinessStoreCollectRequest(
    @NotBlank String commercialAreaCode,
    @Pattern(regexp = "^[A-Za-z0-9]{1,2}$") String largeIndustryCode,
    @Pattern(regexp = "^[A-Za-z0-9]{1,4}$") String mediumIndustryCode,
    @Pattern(regexp = "^[A-Za-z0-9]{1,6}$") String smallIndustryCode,
    boolean force) {

  public SmallBusinessStoreCollectCommand toCommand() {
    return new SmallBusinessStoreCollectCommand(
        commercialAreaCode, largeIndustryCode, mediumIndustryCode, smallIndustryCode, force);
  }
}
