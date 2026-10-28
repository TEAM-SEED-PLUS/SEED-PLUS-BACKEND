package seed.seedplusbackend.payment.application;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.payment.application.command.GmpayPaymentFeedbackCommand;
import seed.seedplusbackend.payment.application.port.GmpayPaymentVerificationPort;
import seed.seedplusbackend.payment.application.result.GmpayPaymentInfo;
import seed.seedplusbackend.payment.domain.entity.Payment;
import seed.seedplusbackend.payment.domain.entity.PaymentOrder;
import seed.seedplusbackend.payment.domain.entity.PaymentOrderStatus;
import seed.seedplusbackend.payment.domain.entity.PaymentProductBillingType;
import seed.seedplusbackend.payment.domain.entity.PaymentProductCode;
import seed.seedplusbackend.payment.domain.entity.PaymentProvider;
import seed.seedplusbackend.payment.domain.entity.PaymentStatus;
import seed.seedplusbackend.payment.domain.entity.ReportPurchaseEntitlement;
import seed.seedplusbackend.payment.domain.entity.Subscription;
import seed.seedplusbackend.payment.domain.entity.SubscriptionStatus;
import seed.seedplusbackend.payment.domain.repository.PaymentOrderRepository;
import seed.seedplusbackend.payment.domain.repository.PaymentRepository;
import seed.seedplusbackend.payment.domain.repository.ReportPurchaseEntitlementRepository;
import seed.seedplusbackend.payment.domain.repository.SubscriptionRepository;

@Service
@RequiredArgsConstructor
public class PaymentFeedbackService {

  private static final ZoneOffset KOREA_STANDARD_TIME = ZoneOffset.ofHours(9);

  private final GmpayPaymentVerificationPort gmpayPaymentVerificationPort;
  private final PaymentOrderRepository paymentOrderRepository;
  private final PaymentRepository paymentRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final ReportPurchaseEntitlementRepository reportPurchaseEntitlementRepository;

  @Transactional
  public void handleFeedback(GmpayPaymentFeedbackCommand command) {
    gmpayPaymentVerificationPort.validateFeedback(command);
    PaymentOrder paymentOrder =
        paymentOrderRepository
            .findByProviderOrderNo(command.orderNo())
            .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_PAYMENT_ORDER));

    if ("CANCEL".equals(command.paymentState())) {
      cancelPaymentOrder(paymentOrder);
      return;
    }
    if (!"COMPLETE".equals(command.paymentState())) {
      throw new ApplicationException(ErrorCode.INVALID_GMPAY_FEEDBACK);
    }
    if (paymentOrder.getStatus() == PaymentOrderStatus.COMPLETED) {
      return;
    }
    if (paymentOrder.getStatus() != PaymentOrderStatus.PAYMENT_REQUESTED) {
      throw new ApplicationException(ErrorCode.GMPAY_PAYMENT_VERIFICATION_FAILED);
    }

    GmpayPaymentInfo paymentInfo = gmpayPaymentVerificationPort.getPaymentInfo(command.orderNo());
    verifyPaymentInfo(paymentOrder, paymentInfo);
    OffsetDateTime completedAt = toOffsetDateTime(paymentInfo.paidAt());
    paymentRepository.save(
        Payment.builder()
            .paymentOrder(paymentOrder)
            .provider(PaymentProvider.GMPAY)
            .providerOrderNo(paymentInfo.providerOrderNo())
            .status(PaymentStatus.COMPLETED)
            .paymentMethod(paymentInfo.payMethod())
            .approvalNo(paymentInfo.approvalNo())
            .receiptUrl(command.receiptUrl())
            .completedAt(completedAt)
            .build());
    grantEntitlement(paymentOrder, completedAt);
    paymentOrder.complete();
  }

  private void cancelPaymentOrder(PaymentOrder paymentOrder) {
    if (paymentOrder.getStatus() == PaymentOrderStatus.PAYMENT_REQUESTED) {
      paymentOrder.cancel();
      return;
    }
    if (paymentOrder.getStatus() != PaymentOrderStatus.CANCELED) {
      throw new ApplicationException(ErrorCode.GMPAY_PAYMENT_VERIFICATION_FAILED);
    }
  }

  private void verifyPaymentInfo(PaymentOrder paymentOrder, GmpayPaymentInfo paymentInfo) {
    if (!"COMPLETE".equals(paymentInfo.state())
        || !paymentOrder.getProviderOrderNo().equals(paymentInfo.providerOrderNo())
        || paymentOrder.getAmount() != paymentInfo.totalPrice()
        || !paymentOrder.getProductName().equals(paymentInfo.goodName())) {
      throw new ApplicationException(ErrorCode.GMPAY_PAYMENT_VERIFICATION_FAILED);
    }
  }

  private void grantEntitlement(PaymentOrder paymentOrder, OffsetDateTime completedAt) {
    if (paymentOrder.getProduct().getBillingType() == PaymentProductBillingType.ONE_TIME) {
      grantReportEntitlement(paymentOrder, completedAt);
      return;
    }
    grantSubscription(paymentOrder, completedAt);
  }

  private void grantReportEntitlement(PaymentOrder paymentOrder, OffsetDateTime completedAt) {
    if (paymentOrder.getReportId() == null) {
      throw new ApplicationException(ErrorCode.GMPAY_PAYMENT_VERIFICATION_FAILED);
    }
    reportPurchaseEntitlementRepository.save(
        ReportPurchaseEntitlement.builder()
            .user(paymentOrder.getUser())
            .reportId(paymentOrder.getReportId())
            .paymentOrder(paymentOrder)
            .grantedAt(completedAt)
            .build());
  }

  private void grantSubscription(PaymentOrder paymentOrder, OffsetDateTime completedAt) {
    OffsetDateTime expiresAt = calculateExpiration(paymentOrder.getProduct().getCode(), completedAt);
    subscriptionRepository.save(
        Subscription.builder()
            .user(paymentOrder.getUser())
            .product(paymentOrder.getProduct())
            .status(SubscriptionStatus.ACTIVE)
            .startedAt(completedAt)
            .expiresAt(expiresAt)
            .nextBillingAt(expiresAt)
            .build());
  }

  private OffsetDateTime calculateExpiration(PaymentProductCode productCode, OffsetDateTime completedAt) {
    return switch (productCode) {
      case PRO_MONTHLY -> completedAt.plusMonths(1);
      case PRO_YEARLY -> completedAt.plusYears(1);
      default -> throw new ApplicationException(ErrorCode.GMPAY_PAYMENT_VERIFICATION_FAILED);
    };
  }

  private OffsetDateTime toOffsetDateTime(LocalDateTime paidAt) {
    if (paidAt == null) {
      return OffsetDateTime.now();
    }
    return paidAt.atOffset(KOREA_STANDARD_TIME);
  }
}
