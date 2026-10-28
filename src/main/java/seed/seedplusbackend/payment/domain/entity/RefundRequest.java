package seed.seedplusbackend.payment.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.global.common.BaseTimeEntity;
import seed.seedplusbackend.global.error.DomainException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.user.domain.entity.User;

@Getter
@Entity
@Table(
    name = "refund_requests",
    uniqueConstraints = {
      @UniqueConstraint(name = "uq_refund_requests_payment", columnNames = "payment_id")
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefundRequest extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "refund_request_id")
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "payment_id", nullable = false)
  private Payment payment;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private RefundRequestStatus status;

  @Column(name = "reason", nullable = false, columnDefinition = "text")
  private String reason;

  @Column(name = "reviewed_at")
  private OffsetDateTime reviewedAt;

  @Column(name = "completed_at")
  private OffsetDateTime completedAt;

  @Column(name = "rejection_reason", columnDefinition = "text")
  private String rejectionReason;

  @Builder
  private RefundRequest(Payment payment, User user, RefundRequestStatus status, String reason) {
    this.payment = payment;
    this.user = user;
    this.status = status;
    this.reason = reason;
  }

  public void startReview(OffsetDateTime reviewedAt) {
    validateStatus(RefundRequestStatus.REQUESTED);
    this.status = RefundRequestStatus.UNDER_REVIEW;
    this.reviewedAt = reviewedAt;
  }

  public void approve(OffsetDateTime reviewedAt) {
    validateStatus(RefundRequestStatus.UNDER_REVIEW);
    this.status = RefundRequestStatus.APPROVED;
    this.reviewedAt = reviewedAt;
  }

  public void reject(String rejectionReason, OffsetDateTime reviewedAt) {
    validateStatus(RefundRequestStatus.UNDER_REVIEW);
    this.status = RefundRequestStatus.REJECTED;
    this.rejectionReason = rejectionReason;
    this.reviewedAt = reviewedAt;
  }

  public void complete(OffsetDateTime completedAt) {
    validateStatus(RefundRequestStatus.APPROVED);
    this.status = RefundRequestStatus.COMPLETED;
    this.completedAt = completedAt;
  }

  private void validateStatus(RefundRequestStatus expectedStatus) {
    if (status != expectedStatus) {
      throw new DomainException(ErrorCode.INVALID_REFUND_REQUEST_STATUS);
    }
  }
}
