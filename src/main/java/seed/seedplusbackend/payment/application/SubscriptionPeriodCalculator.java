package seed.seedplusbackend.payment.application;

import java.time.OffsetDateTime;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.payment.domain.entity.PaymentProductCode;

@Component
public class SubscriptionPeriodCalculator {

  public OffsetDateTime calculateExpiresAt(
      PaymentProductCode productCode, OffsetDateTime startedAt) {
    return switch (productCode) {
      case PRO_MONTHLY -> startedAt.plusMonths(1);
      case PRO_YEARLY -> startedAt.plusYears(1);
      default -> throw new ApplicationException(ErrorCode.INVALID_PAYMENT_ORDER);
    };
  }
}
