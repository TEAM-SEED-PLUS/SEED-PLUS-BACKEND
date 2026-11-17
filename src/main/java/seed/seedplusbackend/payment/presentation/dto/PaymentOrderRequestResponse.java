package seed.seedplusbackend.payment.presentation.dto;

import seed.seedplusbackend.payment.application.result.PaymentOrderRequestResult;

public record PaymentOrderRequestResponse(String orderKey, String providerOrderNo, String payUrl) {

  public static PaymentOrderRequestResponse from(PaymentOrderRequestResult result) {
    return new PaymentOrderRequestResponse(
        result.orderKey(), result.providerOrderNo(), result.payUrl());
  }
}
