package seed.seedplusbackend.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.doNothing;
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
import seed.seedplusbackend.payment.application.command.GmpayPaymentFeedbackCommand;
import seed.seedplusbackend.payment.application.port.GmpayPaymentVerificationPort;
import seed.seedplusbackend.payment.application.result.GmpayPaymentInfo;
import seed.seedplusbackend.payment.domain.entity.Payment;
import seed.seedplusbackend.payment.domain.entity.PaymentOrder;
import seed.seedplusbackend.payment.domain.entity.PaymentOrderStatus;
import seed.seedplusbackend.payment.domain.entity.PaymentProduct;
import seed.seedplusbackend.payment.domain.entity.PaymentProductBillingType;
import seed.seedplusbackend.payment.domain.entity.PaymentProductCode;
import seed.seedplusbackend.payment.domain.entity.ReportPurchaseEntitlement;
import seed.seedplusbackend.payment.domain.entity.Subscription;
import seed.seedplusbackend.payment.domain.repository.PaymentOrderRepository;
import seed.seedplusbackend.payment.domain.repository.PaymentRepository;
import seed.seedplusbackend.payment.domain.repository.ReportPurchaseEntitlementRepository;
import seed.seedplusbackend.payment.domain.repository.SubscriptionRepository;
import seed.seedplusbackend.user.domain.entity.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("결제 통지 서비스")
class PaymentFeedbackServiceTest {

  @Mock private GmpayPaymentVerificationPort gmpayPaymentVerificationPort;
  @Mock private PaymentOrderRepository paymentOrderRepository;
  @Mock private PaymentRepository paymentRepository;
  @Mock private SubscriptionRepository subscriptionRepository;
  @Mock private ReportPurchaseEntitlementRepository reportPurchaseEntitlementRepository;
  @Mock private SubscriptionPeriodCalculator subscriptionPeriodCalculator;
  @Mock private User user;
  @Mock private PaymentProduct product;

  @InjectMocks private PaymentFeedbackService paymentFeedbackService;

  @Test
  @DisplayName("완료된 단건결제는 서버 검증 후 리포트 권한을 부여한다")
  void handleFeedback_grantsReportEntitlementAfterVerification() {
    PaymentOrder paymentOrder = paymentOrder(PaymentProductBillingType.ONE_TIME, PaymentProductCode.SURVIVAL_REPORT);
    given(paymentOrderRepository.findByProviderOrderNo("gmpay-order-1"))
        .willReturn(Optional.of(paymentOrder));
    doNothing().when(gmpayPaymentVerificationPort).validateFeedback(command());
    given(gmpayPaymentVerificationPort.getPaymentInfo("gmpay-order-1"))
        .willReturn(paymentInfo(paymentOrder.getAmount(), paymentOrder.getProductName()));

    paymentFeedbackService.handleFeedback(command());

    ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
    then(paymentRepository).should().save(paymentCaptor.capture());
    then(reportPurchaseEntitlementRepository).should().save(any(ReportPurchaseEntitlement.class));
    assertThat(paymentCaptor.getValue().getProviderOrderNo()).isEqualTo("gmpay-order-1");
    assertThat(paymentOrder.getStatus()).isEqualTo(PaymentOrderStatus.COMPLETED);
  }

  @Test
  @DisplayName("완료된 연간 구독 결제는 1년 유효한 PRO 구독을 생성한다")
  void handleFeedback_createsYearlySubscription() {
    PaymentOrder paymentOrder = paymentOrder(PaymentProductBillingType.SUBSCRIPTION, PaymentProductCode.PRO_YEARLY);
    given(paymentOrderRepository.findByProviderOrderNo("gmpay-order-1"))
        .willReturn(Optional.of(paymentOrder));
    doNothing().when(gmpayPaymentVerificationPort).validateFeedback(command());
    given(gmpayPaymentVerificationPort.getPaymentInfo("gmpay-order-1"))
        .willReturn(paymentInfo(paymentOrder.getAmount(), paymentOrder.getProductName()));
    given(subscriptionPeriodCalculator.calculateExpiresAt(any(), any()))
        .willReturn(OffsetDateTime.parse("2027-10-28T12:00:00+09:00"));

    paymentFeedbackService.handleFeedback(command());

    ArgumentCaptor<Subscription> subscriptionCaptor = ArgumentCaptor.forClass(Subscription.class);
    then(subscriptionRepository).should().save(subscriptionCaptor.capture());
    assertThat(subscriptionCaptor.getValue().getExpiresAt())
        .isEqualTo(OffsetDateTime.parse("2027-10-28T12:00:00+09:00"));
  }

  @Test
  @DisplayName("결제 금액이 주문 정보와 다르면 권한을 부여하지 않는다")
  void handleFeedback_throwsExceptionWhenAmountDoesNotMatch() {
    PaymentOrder paymentOrder = paymentOrder(PaymentProductBillingType.ONE_TIME, PaymentProductCode.SURVIVAL_REPORT);
    given(paymentOrderRepository.findByProviderOrderNo("gmpay-order-1"))
        .willReturn(Optional.of(paymentOrder));
    doNothing().when(gmpayPaymentVerificationPort).validateFeedback(command());
    given(gmpayPaymentVerificationPort.getPaymentInfo("gmpay-order-1"))
        .willReturn(paymentInfo(1L, "생존율 상세 리포트"));

    assertThatThrownBy(() -> paymentFeedbackService.handleFeedback(command()))
        .isInstanceOfSatisfying(
            ApplicationException.class,
            exception ->
                assertThat(exception.getErrorCode())
                    .isEqualTo(ErrorCode.GMPAY_PAYMENT_VERIFICATION_FAILED));
    then(paymentRepository).shouldHaveNoInteractions();
    then(reportPurchaseEntitlementRepository).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("이미 완료된 주문의 중복 통지는 권한을 다시 부여하지 않는다")
  void handleFeedback_doesNotGrantEntitlementAgainWhenOrderIsCompleted() {
    PaymentOrder paymentOrder = paymentOrder(PaymentProductBillingType.ONE_TIME, PaymentProductCode.SURVIVAL_REPORT);
    paymentOrder.complete();
    given(paymentOrderRepository.findByProviderOrderNo("gmpay-order-1"))
        .willReturn(Optional.of(paymentOrder));
    doNothing().when(gmpayPaymentVerificationPort).validateFeedback(command());

    paymentFeedbackService.handleFeedback(command());

    then(gmpayPaymentVerificationPort).should().validateFeedback(command());
    then(gmpayPaymentVerificationPort).shouldHaveNoMoreInteractions();
    then(paymentRepository).shouldHaveNoInteractions();
    then(reportPurchaseEntitlementRepository).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("취소 통지는 주문을 취소 상태로만 변경한다")
  void handleFeedback_cancelsPaymentOrder() {
    PaymentOrder paymentOrder = paymentOrder(PaymentProductBillingType.ONE_TIME, PaymentProductCode.SURVIVAL_REPORT);
    GmpayPaymentFeedbackCommand cancelCommand =
        new GmpayPaymentFeedbackCommand(
            "seed-member", "feedback-token", "CANCEL", "gmpay-order-1", null, null, null);
    given(paymentOrderRepository.findByProviderOrderNo("gmpay-order-1"))
        .willReturn(Optional.of(paymentOrder));
    doNothing().when(gmpayPaymentVerificationPort).validateFeedback(cancelCommand);

    paymentFeedbackService.handleFeedback(cancelCommand);

    assertThat(paymentOrder.getStatus()).isEqualTo(PaymentOrderStatus.CANCELED);
    then(gmpayPaymentVerificationPort).should().validateFeedback(cancelCommand);
    then(gmpayPaymentVerificationPort).shouldHaveNoMoreInteractions();
  }

  private PaymentOrder paymentOrder(
      PaymentProductBillingType billingType, PaymentProductCode productCode) {
    boolean reportProduct = billingType == PaymentProductBillingType.ONE_TIME;
    String productName = reportProduct ? "생존율 상세 리포트" : "PRO 연간 구독";
    long amount = reportProduct ? 9000L : 59000L;
    given(product.getBillingType()).willReturn(billingType);
    given(product.getCode()).willReturn(productCode);
    PaymentOrder paymentOrder =
        PaymentOrder.builder()
            .user(user)
            .product(product)
            .orderKey("order-key")
            .reportId(reportProduct ? "report-1" : null)
            .productName(productName)
            .amount(amount)
            .status(PaymentOrderStatus.READY)
            .build();
    paymentOrder.markPaymentRequested("gmpay-order-1", OffsetDateTime.now());
    return paymentOrder;
  }

  private GmpayPaymentFeedbackCommand command() {
    return new GmpayPaymentFeedbackCommand(
        "seed-member",
        "feedback-token",
        "COMPLETE",
        "gmpay-order-1",
        "CARD",
        "approval-no",
        "https://pay.gmpay.kr/receipt");
  }

  private GmpayPaymentInfo paymentInfo(long totalPrice, String goodName) {
    return new GmpayPaymentInfo(
        "gmpay-order-1",
        "COMPLETE",
        totalPrice,
        goodName,
        "seed-member",
        "CARD",
        "approval-no",
        LocalDateTime.of(2026, 10, 28, 12, 0));
  }
}
