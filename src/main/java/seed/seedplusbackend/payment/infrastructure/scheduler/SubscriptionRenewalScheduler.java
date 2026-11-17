package seed.seedplusbackend.payment.infrastructure.scheduler;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.payment.application.SubscriptionRecurringPaymentService;
import seed.seedplusbackend.payment.domain.entity.Subscription;
import seed.seedplusbackend.payment.domain.entity.SubscriptionStatus;
import seed.seedplusbackend.payment.domain.repository.SubscriptionRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionRenewalScheduler {

  private final SubscriptionRenewalProperties properties;
  private final SubscriptionRepository subscriptionRepository;
  private final SubscriptionRecurringPaymentService subscriptionRecurringPaymentService;

  @Scheduled(fixedDelayString = "${payment.subscription-renewal.interval-millis:300000}")
  public void renewDueSubscriptions() {
    if (!properties.enabled()) {
      return;
    }

    OffsetDateTime now = OffsetDateTime.now();
    List<Subscription> subscriptions =
        subscriptionRepository.findByStatusAndNextBillingAtLessThanEqualOrderByNextBillingAtAsc(
            SubscriptionStatus.ACTIVE, now, PageRequest.of(0, properties.batchSize()));
    for (Subscription subscription : subscriptions) {
      try {
        subscriptionRecurringPaymentService.renewDueSubscription(subscription.getId(), now);
      } catch (RuntimeException exception) {
        log.error(
            "[SubscriptionRenewalScheduler] 구독 정기결제 처리 실패, subscriptionId={}",
            subscription.getId(),
            exception);
      }
    }
  }
}
