package seed.seedplusbackend.payment.application;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import seed.seedplusbackend.payment.domain.entity.PaymentProvider;
import seed.seedplusbackend.payment.domain.entity.PaymentStatus;
import seed.seedplusbackend.payment.domain.entity.Subscription;
import seed.seedplusbackend.payment.domain.repository.PaymentOrderRepository;
import seed.seedplusbackend.payment.domain.repository.PaymentRepository;
import seed.seedplusbackend.payment.domain.repository.SubscriptionRepository;

@Service
@RequiredArgsConstructor
public class SubscriptionRecurringPaymentService {

  private static final ZoneOffset KOREA_STANDARD_TIME = ZoneOffset.ofHours(9);

  private final SubscriptionRepository subscriptionRepository;
  private final PaymentOrderRepository paymentOrderRepository;
  private final PaymentRepository paymentRepository;
  private final BillingKeyEncryptionPort billingKeyEncryptionPort;
  private final GmpayRecurringPaymentPort gmpayRecurringPaymentPort;
  private final SubscriptionPeriodCalculator subscriptionPeriodCalculator;

  @Transactional
  public RecurringPaymentResult requestInitialPayment(Long userId, Long subscriptionId) {
    Subscription subscription =
        subscriptionRepository
            .findByIdAndUser_Id(subscriptionId, userId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_SUBSCRIPTION));
    if (!subscription.isReadyForInitialPayment()) {
      throw new ApplicationException(ErrorCode.INVALID_SUBSCRIPTION_STATUS);
    }

    String billingKey = billingKeyEncryptionPort.decrypt(subscription.getEncryptedBillingKey());
    PaymentOrder paymentOrder =
        paymentOrderRepository.save(
            PaymentOrder.builder()
                .user(subscription.getUser())
                .product(subscription.getProduct())
                .orderKey(UUID.randomUUID().toString())
                .productName(subscription.getProduct().getName())
                .amount(subscription.getProduct().getAmount())
                .status(PaymentOrderStatus.READY)
                .build());
    GmpayRecurringPaymentResult paymentResult;
    try {
      paymentResult =
          gmpayRecurringPaymentPort.requestPayment(
              new GmpayRecurringPaymentCommand(
                  billingKey,
                  paymentOrder.getAmount(),
                  paymentOrder.getProductName(),
                  subscription.getUser().getPhoneNumber(),
                  subscription.getUser().getEmail(),
                  subscription.getUser().getName(),
                  paymentOrder.getOrderKey(),
                  subscription.getProduct().getCode().name()));
    } catch (ApplicationException exception) {
      paymentOrder.fail();
      subscription.markPaymentFailed();
      return new RecurringPaymentResult(false, null);
    }
    if (paymentResult.amount() != paymentOrder.getAmount()) {
      paymentOrder.fail();
      subscription.markPaymentFailed();
      return new RecurringPaymentResult(false, null);
    }

    OffsetDateTime completedAt = toOffsetDateTime(paymentResult.paidAt());
    paymentOrder.markPaymentRequested(paymentResult.providerOrderNo(), completedAt);
    paymentRepository.save(
        Payment.builder()
            .paymentOrder(paymentOrder)
            .provider(PaymentProvider.GMPAY)
            .providerOrderNo(paymentResult.providerOrderNo())
            .status(PaymentStatus.COMPLETED)
            .paymentMethod(paymentResult.paymentMethod())
            .approvalNo(paymentResult.approvalNo())
            .receiptUrl(paymentResult.receiptUrl())
            .completedAt(completedAt)
            .build());
    OffsetDateTime expiresAt =
        subscriptionPeriodCalculator.calculateExpiresAt(subscription.getProduct().getCode(), completedAt);
    subscription.activate(completedAt, expiresAt, expiresAt);
    paymentOrder.complete();
    return new RecurringPaymentResult(true, paymentResult.providerOrderNo());
  }

  private OffsetDateTime toOffsetDateTime(LocalDateTime paidAt) {
    if (paidAt == null) {
      return OffsetDateTime.now();
    }
    return paidAt.atOffset(KOREA_STANDARD_TIME);
  }
}
