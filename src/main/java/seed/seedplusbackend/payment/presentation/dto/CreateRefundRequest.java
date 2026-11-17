package seed.seedplusbackend.payment.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import seed.seedplusbackend.payment.application.command.CreateRefundRequestCommand;

@Schema(description = "환불 신청 요청")
public record CreateRefundRequest(
    @NotNull @Schema(description = "결제 ID") Long paymentId,
    @NotBlank @Schema(description = "환불 신청 사유") String reason) {

  public CreateRefundRequestCommand toCommand() {
    return new CreateRefundRequestCommand(paymentId, reason);
  }
}
