package seed.seedplusbackend.payment.application.result;

import java.time.OffsetDateTime;
import seed.seedplusbackend.payment.domain.entity.PaymentProductCode;
import seed.seedplusbackend.payment.domain.entity.SubscriptionStatus;

public record SubscriptionStatusResult(
    Long subscriptionId,
    PaymentProductCode productCode,
    SubscriptionStatus status,
    boolean active,
    OffsetDateTime startedAt,
    OffsetDateTime expiresAt,
    OffsetDateTime nextBillingAt) {

  public static SubscriptionStatusResult empty() {
    return new SubscriptionStatusResult(null, null, null, false, null, null, null);
  }
}
