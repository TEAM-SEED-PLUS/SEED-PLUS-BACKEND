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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.global.common.BaseTimeEntity;

@Getter
@Entity
@Table(
    name = "payments",
    uniqueConstraints = {
      @UniqueConstraint(name = "uq_payments_payment_order", columnNames = "payment_order_id"),
      @UniqueConstraint(name = "uq_payments_provider_order_no", columnNames = "provider_order_no")
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "payment_id")
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "payment_order_id", nullable = false)
  private PaymentOrder paymentOrder;

  @Enumerated(EnumType.STRING)
  @Column(name = "provider", nullable = false, length = 20)
  private PaymentProvider provider;

  @Column(name = "provider_order_no", nullable = false, length = 100)
  private String providerOrderNo;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private PaymentStatus status;

  @Column(name = "payment_method", length = 30)
  private String paymentMethod;

  @Column(name = "approval_no", length = 100)
  private String approvalNo;

  @Column(name = "receipt_url", length = 1000)
  private String receiptUrl;

  @Column(name = "completed_at")
  private OffsetDateTime completedAt;

  @Column(name = "canceled_at")
  private OffsetDateTime canceledAt;

  @Builder
  private Payment(
      PaymentOrder paymentOrder,
      PaymentProvider provider,
      String providerOrderNo,
      PaymentStatus status,
      String paymentMethod,
      String approvalNo,
      String receiptUrl,
      OffsetDateTime completedAt) {
    this.paymentOrder = paymentOrder;
    this.provider = provider;
    this.providerOrderNo = providerOrderNo;
    this.status = status;
    this.paymentMethod = paymentMethod;
    this.approvalNo = approvalNo;
    this.receiptUrl = receiptUrl;
    this.completedAt = completedAt;
  }

  public void cancel(OffsetDateTime canceledAt) {
    this.status = PaymentStatus.CANCELED;
    this.canceledAt = canceledAt;
  }
}
