package seed.seedplusbackend.payment.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import seed.seedplusbackend.payment.application.command.CreateCardRegistrationCommand;
import seed.seedplusbackend.payment.domain.entity.PaymentProductCode;

@Schema(description = "GMPay 카드 등록 URL 발급 요청")
public record CardRegistrationRequest(
    @NotNull @Schema(description = "구독 상품") PaymentProductCode productCode) {

  public CreateCardRegistrationCommand toCommand() {
    return new CreateCardRegistrationCommand(productCode);
  }
}
