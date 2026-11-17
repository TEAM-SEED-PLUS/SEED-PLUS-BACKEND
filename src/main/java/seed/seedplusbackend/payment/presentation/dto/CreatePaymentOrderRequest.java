package seed.seedplusbackend.payment.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import seed.seedplusbackend.payment.application.command.CreatePaymentOrderCommand;
import seed.seedplusbackend.payment.domain.entity.PaymentProductCode;

@Schema(description = "GMPay 결제 요청")
public record CreatePaymentOrderRequest(
    @NotNull @Schema(description = "결제 상품") PaymentProductCode productCode,
    @Schema(description = "생존율 상세 리포트 식별값") String reportId) {

  public CreatePaymentOrderCommand toCommand() {
    return new CreatePaymentOrderCommand(productCode, reportId);
  }
}
