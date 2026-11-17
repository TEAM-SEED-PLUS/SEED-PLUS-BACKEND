package seed.seedplusbackend.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.payment.application.command.GmpayRecurringPaymentCommand;
import seed.seedplusbackend.payment.application.port.BillingKeyEncryptionPort;
import seed.seedplusbackend.payment.application.port.GmpayRecurringPaymentPort;
import seed.seedplusbackend.payment.application.result.GmpayRecurringPaymentResult;
import seed.seedplusbackend.payment.application.result.RecurringPaymentResult;
import seed.seedplusbackend.payment.domain.entity.Payment;
import seed.seedplusbackend.payment.domain.entity.PaymentOrder;
import seed.seedplusbackend.payment.domain.entity.PaymentOrderStatus;
import seed.seedplusbackend.payment.domain.entity.PaymentProduct;
import seed.seedplusbackend.payment.domain.entity.PaymentProductCode;
import seed.seedplusbackend.payment.domain.entity.Subscription;
import seed.seedplusbackend.payment.domain.entity.SubscriptionStatus;
import seed.seedplusbackend.payment.domain.repository.PaymentOrderRepository;
import seed.seedplusbackend.payment.domain.repository.PaymentRepository;
import seed.seedplusbackend.payment.domain.repository.SubscriptionRepository;
import seed.seedplusbackend.user.domain.entity.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("구독 등록결제 서비스")
class SubscriptionRecurringPaymentServiceTest {

  @Mock private SubscriptionRepository subscriptionRepository;
  @Mock private PaymentOrderRepository paymentOrderRepository;
  @Mock private PaymentRepository paymentRepository;
  @Mock private BillingKeyEncryptionPort billingKeyEncryptionPort;
  @Mock private GmpayRecurringPaymentPort gmpayRecurringPaymentPort;
  @Mock private SubscriptionPeriodCalculator subscriptionPeriodCalculator;
  @Mock private User user;
  @Mock private PaymentProduct product;

  @InjectMocks private SubscriptionRecurringPaymentService subscriptionRecurringPaymentService;

  @Test
  @DisplayName("등록결제 성공 시 결제를 저장하고 구독을 활성화한다")
  void requestInitialPayment_activatesSubscriptionWhenPaymentSucceeds() {
    Subscription subscription = pendingSubscription();
    given(subscriptionRepository.findByIdAndUser_Id(1L, 1L)).willReturn(Optional.of(subscription));
    given(paymentOrderRepository.save(any(PaymentOrder.class)))
        .willAnswer(invocation -> invocation.getArgument(0));
    given(billingKeyEncryptionPort.decrypt("encrypted-mp-key")).willReturn("mp-key");
    given(gmpayRecurringPaymentPort.requestPayment(any()))
        .willReturn(
            new GmpayRecurringPaymentResult(
                "gmpay-order-1",
                5900L,
                "CARD",
                "approval-no",
                "https://pay.gmpay.kr/receipt",
                LocalDateTime.of(2026, 11, 3, 12, 0)));
    OffsetDateTime expiresAt = OffsetDateTime.parse("2026-12-03T12:00:00+09:00");
    given(subscriptionPeriodCalculator.calculateExpiresAt(any(), any())).willReturn(expiresAt);

    RecurringPaymentResult result =
        subscriptionRecurringPaymentService.requestInitialPayment(1L, 1L);

    ArgumentCaptor<GmpayRecurringPaymentCommand> commandCaptor =
        ArgumentCaptor.forClass(GmpayRecurringPaymentCommand.class);
    ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
    then(gmpayRecurringPaymentPort).should().requestPayment(commandCaptor.capture());
    then(paymentRepository).should().save(paymentCaptor.capture());
    assertThat(commandCaptor.getValue().mpKey()).isEqualTo("mp-key");
    assertThat(commandCaptor.getValue().totalPrice()).isEqualTo(5900L);
    assertThat(paymentCaptor.getValue().getProviderOrderNo()).isEqualTo("gmpay-order-1");
    assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    assertThat(subscription.getExpiresAt()).isEqualTo(expiresAt);
    assertThat(result).isEqualTo(new RecurringPaymentResult(true, "gmpay-order-1"));
  }

  @Test
  @DisplayName("등록결제 실패 시 결제를 완료 처리하지 않고 구독을 결제 실패 상태로 변경한다")
  void requestInitialPayment_marksSubscriptionAsPaymentFailedWhenPaymentFails() {
    Subscription subscription = pendingSubscription();
    given(subscriptionRepository.findByIdAndUser_Id(1L, 1L)).willReturn(Optional.of(subscription));
    given(paymentOrderRepository.save(any(PaymentOrder.class)))
        .willAnswer(invocation -> invocation.getArgument(0));
    given(billingKeyEncryptionPort.decrypt("encrypted-mp-key")).willReturn("mp-key");
    given(gmpayRecurringPaymentPort.requestPayment(any()))
        .willThrow(new ApplicationException(ErrorCode.GMPAY_RECURRING_PAYMENT_FAILED));

    RecurringPaymentResult result =
        subscriptionRecurringPaymentService.requestInitialPayment(1L, 1L);

    ArgumentCaptor<PaymentOrder> paymentOrderCaptor = ArgumentCaptor.forClass(PaymentOrder.class);
    then(paymentOrderRepository).should().save(paymentOrderCaptor.capture());
    then(paymentRepository).shouldHaveNoInteractions();
    assertThat(paymentOrderCaptor.getValue().getStatus()).isEqualTo(PaymentOrderStatus.FAILED);
    assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.PAYMENT_FAILED);
    assertThat(result).isEqualTo(new RecurringPaymentResult(false, null));
  }

  private Subscription pendingSubscription() {
    given(product.getName()).willReturn("PRO 월간 구독");
    given(product.getAmount()).willReturn(5900L);
    given(product.getCode()).willReturn(PaymentProductCode.PRO_MONTHLY);
    given(user.getPhoneNumber()).willReturn("01012345678");
    given(user.getEmail()).willReturn("user@example.com");
    return Subscription.builder()
        .user(user)
        .product(product)
        .status(SubscriptionStatus.PENDING)
        .encryptedBillingKey("encrypted-mp-key")
        .build();
  }
}
