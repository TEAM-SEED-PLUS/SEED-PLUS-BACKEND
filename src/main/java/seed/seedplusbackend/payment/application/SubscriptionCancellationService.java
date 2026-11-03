package seed.seedplusbackend.payment.application;

import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.payment.application.result.SubscriptionStatusResult;
import seed.seedplusbackend.payment.domain.entity.Subscription;
import seed.seedplusbackend.payment.domain.repository.SubscriptionRepository;

@Service
@RequiredArgsConstructor
public class SubscriptionCancellationService {

  private final SubscriptionRepository subscriptionRepository;

  @Transactional
  public SubscriptionStatusResult cancel(Long userId, Long subscriptionId, OffsetDateTime now) {
    Subscription subscription =
        subscriptionRepository
            .findByIdAndUser_Id(subscriptionId, userId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_SUBSCRIPTION));
    try {
      subscription.cancel(now);
    } catch (IllegalStateException exception) {
      throw new ApplicationException(ErrorCode.INVALID_SUBSCRIPTION_STATUS, exception);
    }

    return new SubscriptionStatusResult(
        subscription.getId(),
        subscription.getProduct().getCode(),
        subscription.getStatus(),
        subscription.isActiveAt(now),
        subscription.getStartedAt(),
        subscription.getExpiresAt(),
        subscription.getNextBillingAt(),
        subscription.getCanceledAt());
  }
}
