package seed.seedplusbackend.commercial.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import seed.seedplusbackend.commercial.application.command.CommercialEstimatedSalesCollectCommand;

public record CommercialEstimatedSalesCollectRequest(
    @NotBlank @Pattern(regexp = "^20\\d{2}[1-4]$", message = "기준년분기 코드는 예: 20251 형식이어야 합니다.")
        String stdrYyquCd,
    boolean force) {

  public CommercialEstimatedSalesCollectCommand toCommand() {
    return new CommercialEstimatedSalesCollectCommand(stdrYyquCd, force);
  }
}
