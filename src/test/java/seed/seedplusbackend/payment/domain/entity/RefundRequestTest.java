package seed.seedplusbackend.payment.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import seed.seedplusbackend.global.error.DomainException;
import seed.seedplusbackend.global.error.ErrorCode;

@DisplayName("환불 요청")
class RefundRequestTest {

  @Test
  @DisplayName("환불 요청은 검토와 승인 후에 완료할 수 있다")
  void complete_changesStatusAfterReviewAndApproval() {
    OffsetDateTime now = OffsetDateTime.parse("2026-10-28T12:00:00+09:00");
    RefundRequest refundRequest = RefundRequest.builder().status(RefundRequestStatus.REQUESTED).build();

    refundRequest.startReview(now);
    refundRequest.approve(now.plusMinutes(1));
    refundRequest.complete(now.plusMinutes(2));

    assertThat(refundRequest.getStatus()).isEqualTo(RefundRequestStatus.COMPLETED);
    assertThat(refundRequest.getCompletedAt()).isEqualTo(now.plusMinutes(2));
  }

  @Test
  @DisplayName("승인되지 않은 환불 요청은 완료할 수 없다")
  void complete_throwsExceptionBeforeApproval() {
    RefundRequest refundRequest = RefundRequest.builder().status(RefundRequestStatus.REQUESTED).build();

    assertThatThrownBy(
            () -> refundRequest.complete(OffsetDateTime.parse("2026-10-28T12:00:00+09:00")))
        .isInstanceOfSatisfying(
            DomainException.class,
            exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REFUND_REQUEST_STATUS));
  }
}
