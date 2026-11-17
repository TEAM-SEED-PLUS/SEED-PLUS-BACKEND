package seed.seedplusbackend.payment.presentation.dto;

import java.time.OffsetDateTime;
import seed.seedplusbackend.payment.application.result.PaymentHistoryResult;
import seed.seedplusbackend.payment.domain.entity.PaymentProductCode;
import seed.seedplusbackend.payment.domain.entity.PaymentStatus;

public record PaymentHistoryResponse(
    Long paymentId,
    PaymentProductCode productCode,
    String productName,
    long amount,
    PaymentStatus status,
    String paymentMethod,
    String receiptUrl,
    OffsetDateTime completedAt) {

  public static PaymentHistoryResponse from(PaymentHistoryResult result) {
    return new PaymentHistoryResponse(
        result.paymentId(),
        result.productCode(),
        result.productName(),
        result.amount(),
        result.status(),
        result.paymentMethod(),
        result.receiptUrl(),
        result.completedAt());
  }
}
