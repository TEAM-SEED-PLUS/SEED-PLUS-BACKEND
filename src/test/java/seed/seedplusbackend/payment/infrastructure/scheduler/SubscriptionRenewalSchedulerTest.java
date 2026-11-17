package seed.seedplusbackend.payment.infrastructure.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seed.seedplusbackend.payment.application.SubscriptionRecurringPaymentService;
import seed.seedplusbackend.payment.domain.entity.Subscription;
import seed.seedplusbackend.payment.domain.repository.SubscriptionRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("구독 정기결제 스케줄러")
class SubscriptionRenewalSchedulerTest {

  @Mock private SubscriptionRepository subscriptionRepository;
  @Mock private SubscriptionRecurringPaymentService subscriptionRecurringPaymentService;
  @Mock private Subscription subscription;

  @Test
  @DisplayName("비활성화 상태에서는 결제 대상을 조회하지 않는다")
  void renewDueSubscriptions_doesNotQueryWhenDisabled() {
    SubscriptionRenewalScheduler scheduler =
        new SubscriptionRenewalScheduler(
            new SubscriptionRenewalProperties(false, 50),
            subscriptionRepository,
            subscriptionRecurringPaymentService);

    scheduler.renewDueSubscriptions();

    then(subscriptionRepository).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("활성화 상태에서는 만기 구독을 하나씩 정기결제 처리한다")
  void renewDueSubscriptions_renewsDueSubscriptionsWhenEnabled() {
    SubscriptionRenewalScheduler scheduler =
        new SubscriptionRenewalScheduler(
            new SubscriptionRenewalProperties(true, 50),
            subscriptionRepository,
            subscriptionRecurringPaymentService);
    given(subscription.getId()).willReturn(1L);
    given(
            subscriptionRepository.findByStatusAndNextBillingAtLessThanEqualOrderByNextBillingAtAsc(
                any(), any(), any()))
        .willReturn(List.of(subscription));

    scheduler.renewDueSubscriptions();

    then(subscriptionRecurringPaymentService).should().renewDueSubscription(any(), any());
  }
}
