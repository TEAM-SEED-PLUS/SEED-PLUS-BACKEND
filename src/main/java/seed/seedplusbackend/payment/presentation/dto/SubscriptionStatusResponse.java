package seed.seedplusbackend.payment.presentation.dto;

import java.time.OffsetDateTime;
import seed.seedplusbackend.payment.application.result.SubscriptionStatusResult;
import seed.seedplusbackend.payment.domain.entity.PaymentProductCode;
import seed.seedplusbackend.payment.domain.entity.SubscriptionStatus;

public record SubscriptionStatusResponse(
    Long subscriptionId,
    PaymentProductCode productCode,
    SubscriptionStatus status,
    boolean active,
    OffsetDateTime startedAt,
    OffsetDateTime expiresAt,
    OffsetDateTime nextBillingAt,
    OffsetDateTime canceledAt) {

  public static SubscriptionStatusResponse from(SubscriptionStatusResult result) {
    return new SubscriptionStatusResponse(
        result.subscriptionId(),
        result.productCode(),
        result.status(),
        result.active(),
        result.startedAt(),
        result.expiresAt(),
        result.nextBillingAt(),
        result.canceledAt());
  }
}
