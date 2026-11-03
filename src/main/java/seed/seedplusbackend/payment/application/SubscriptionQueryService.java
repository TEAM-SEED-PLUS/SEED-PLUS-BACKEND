package seed.seedplusbackend.payment.application;

import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.payment.application.result.SubscriptionStatusResult;
import seed.seedplusbackend.payment.domain.repository.SubscriptionRepository;

@Service
@RequiredArgsConstructor
public class SubscriptionQueryService {

  private final SubscriptionRepository subscriptionRepository;

  @Transactional(readOnly = true)
  public SubscriptionStatusResult getLatestSubscription(Long userId, OffsetDateTime now) {
    return subscriptionRepository
        .findFirstByUser_IdOrderByCreatedAtDesc(userId)
        .map(
            subscription ->
                new SubscriptionStatusResult(
                    subscription.getId(),
                    subscription.getProduct().getCode(),
                    subscription.getStatus(),
                    subscription.isActiveAt(now),
                    subscription.getStartedAt(),
                    subscription.getExpiresAt(),
                    subscription.getNextBillingAt()))
        .orElse(SubscriptionStatusResult.empty());
  }
}
