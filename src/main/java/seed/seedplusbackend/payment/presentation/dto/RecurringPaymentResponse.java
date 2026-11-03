package seed.seedplusbackend.payment.presentation.dto;

import seed.seedplusbackend.payment.application.result.RecurringPaymentResult;

public record RecurringPaymentResponse(boolean success, String providerOrderNo) {

  public static RecurringPaymentResponse from(RecurringPaymentResult result) {
    return new RecurringPaymentResponse(result.success(), result.providerOrderNo());
  }
}
