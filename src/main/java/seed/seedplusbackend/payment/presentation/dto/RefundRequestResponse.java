package seed.seedplusbackend.payment.presentation.dto;

import java.time.OffsetDateTime;
import seed.seedplusbackend.payment.application.result.RefundRequestResult;
import seed.seedplusbackend.payment.domain.entity.RefundRequestStatus;

public record RefundRequestResponse(
    Long refundRequestId,
    Long paymentId,
    RefundRequestStatus status,
    String reason,
    OffsetDateTime requestedAt,
    OffsetDateTime reviewedAt,
    OffsetDateTime completedAt,
    String rejectionReason) {

  public static RefundRequestResponse from(RefundRequestResult result) {
    return new RefundRequestResponse(
        result.refundRequestId(),
        result.paymentId(),
        result.status(),
        result.reason(),
        result.requestedAt(),
        result.reviewedAt(),
        result.completedAt(),
        result.rejectionReason());
  }
}
