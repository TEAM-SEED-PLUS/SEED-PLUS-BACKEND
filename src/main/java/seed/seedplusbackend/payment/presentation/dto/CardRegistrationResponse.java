package seed.seedplusbackend.payment.presentation.dto;

import seed.seedplusbackend.payment.application.result.CardRegistrationRequestResult;

public record CardRegistrationResponse(Long subscriptionId, String payUrl) {

  public static CardRegistrationResponse from(CardRegistrationRequestResult result) {
    return new CardRegistrationResponse(result.subscriptionId(), result.payUrl());
  }
}
