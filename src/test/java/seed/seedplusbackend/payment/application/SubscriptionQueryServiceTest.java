package seed.seedplusbackend.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seed.seedplusbackend.payment.application.result.SubscriptionStatusResult;
import seed.seedplusbackend.payment.domain.entity.PaymentProduct;
import seed.seedplusbackend.payment.domain.entity.PaymentProductCode;
import seed.seedplusbackend.payment.domain.entity.Subscription;
import seed.seedplusbackend.payment.domain.entity.SubscriptionStatus;
import seed.seedplusbackend.payment.domain.repository.SubscriptionRepository;
import seed.seedplusbackend.user.domain.entity.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("구독 조회 서비스")
class SubscriptionQueryServiceTest {

  @Mock private SubscriptionRepository subscriptionRepository;
  @Mock private PaymentProduct product;
  @Mock private User user;

  @InjectMocks private SubscriptionQueryService subscriptionQueryService;

  @Test
  @DisplayName("구독이 없으면 FREE 상태를 반환한다")
  void getLatestSubscription_returnsInactiveResultWhenSubscriptionDoesNotExist() {
    given(subscriptionRepository.findFirstByUser_IdOrderByCreatedAtDesc(1L)).willReturn(Optional.empty());

    SubscriptionStatusResult result =
        subscriptionQueryService.getLatestSubscription(1L, OffsetDateTime.now());

    assertThat(result.active()).isFalse();
    assertThat(result.status()).isNull();
  }

  @Test
  @DisplayName("활성 구독의 상태와 만료일을 반환한다")
  void getLatestSubscription_returnsActiveSubscription() {
    OffsetDateTime now = OffsetDateTime.parse("2026-11-03T12:00:00+09:00");
    Subscription subscription =
        Subscription.builder()
            .user(user)
            .product(product)
            .status(SubscriptionStatus.ACTIVE)
            .startedAt(now.minusDays(1))
            .expiresAt(now.plusDays(29))
            .nextBillingAt(now.plusDays(29))
            .build();
    given(product.getCode()).willReturn(PaymentProductCode.PRO_MONTHLY);
    given(subscriptionRepository.findFirstByUser_IdOrderByCreatedAtDesc(1L))
        .willReturn(Optional.of(subscription));

    SubscriptionStatusResult result = subscriptionQueryService.getLatestSubscription(1L, now);

    assertThat(result.productCode()).isEqualTo(PaymentProductCode.PRO_MONTHLY);
    assertThat(result.status()).isEqualTo(SubscriptionStatus.ACTIVE);
    assertThat(result.active()).isTrue();
  }
}
