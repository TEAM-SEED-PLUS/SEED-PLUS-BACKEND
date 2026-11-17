package seed.seedplusbackend.payment.application.result;

import java.time.OffsetDateTime;
import seed.seedplusbackend.payment.domain.entity.RefundRequest;
import seed.seedplusbackend.payment.domain.entity.RefundRequestStatus;

public record RefundRequestResult(
    Long refundRequestId,
    Long paymentId,
    RefundRequestStatus status,
    String reason,
    OffsetDateTime requestedAt,
    OffsetDateTime reviewedAt,
    OffsetDateTime completedAt,
    String rejectionReason) {

  public static RefundRequestResult from(RefundRequest refundRequest) {
    return new RefundRequestResult(
        refundRequest.getId(),
        refundRequest.getPayment().getId(),
        refundRequest.getStatus(),
        refundRequest.getReason(),
        refundRequest.getCreatedAt(),
        refundRequest.getReviewedAt(),
        refundRequest.getCompletedAt(),
        refundRequest.getRejectionReason());
  }
}
