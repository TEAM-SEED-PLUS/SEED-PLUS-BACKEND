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
@DisplayName("구독 해지 서비스")
class SubscriptionCancellationServiceTest {

  @Mock private SubscriptionRepository subscriptionRepository;
  @Mock private PaymentProduct product;
  @Mock private User user;

  @InjectMocks private SubscriptionCancellationService subscriptionCancellationService;

  @Test
  @DisplayName("해지하면 다음 결제를 중단하고 만료 전 권한을 유지한다")
  void cancel_stopsNextBillingAndKeepsAccessUntilExpiration() {
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
    given(subscriptionRepository.findByIdAndUser_Id(1L, 1L)).willReturn(Optional.of(subscription));
    given(product.getCode()).willReturn(PaymentProductCode.PRO_MONTHLY);

    SubscriptionStatusResult result = subscriptionCancellationService.cancel(1L, 1L, now);

    assertThat(result.status()).isEqualTo(SubscriptionStatus.CANCELED);
    assertThat(result.active()).isTrue();
    assertThat(result.expiresAt()).isEqualTo(now.plusDays(29));
    assertThat(result.nextBillingAt()).isNull();
    assertThat(result.canceledAt()).isEqualTo(now);
  }
}
