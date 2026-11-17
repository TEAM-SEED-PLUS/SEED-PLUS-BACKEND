package seed.seedplusbackend.payment.application.result;

import java.time.OffsetDateTime;
import seed.seedplusbackend.payment.domain.entity.Payment;
import seed.seedplusbackend.payment.domain.entity.PaymentProductCode;
import seed.seedplusbackend.payment.domain.entity.PaymentStatus;

public record PaymentHistoryResult(
    Long paymentId,
    PaymentProductCode productCode,
    String productName,
    long amount,
    PaymentStatus status,
    String paymentMethod,
    String receiptUrl,
    OffsetDateTime completedAt) {

  public static PaymentHistoryResult from(Payment payment) {
    return new PaymentHistoryResult(
        payment.getId(),
        payment.getPaymentOrder().getProduct().getCode(),
        payment.getPaymentOrder().getProductName(),
        payment.getPaymentOrder().getAmount(),
        payment.getStatus(),
        payment.getPaymentMethod(),
        payment.getReceiptUrl(),
        payment.getCompletedAt());
  }
}
