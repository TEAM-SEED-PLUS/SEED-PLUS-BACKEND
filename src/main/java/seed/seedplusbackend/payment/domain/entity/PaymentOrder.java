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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.global.common.BaseTimeEntity;
import seed.seedplusbackend.user.domain.entity.User;

@Getter
@Entity
@Table(
    name = "payment_orders",
    uniqueConstraints = {
      @UniqueConstraint(name = "uq_payment_orders_order_key", columnNames = "order_key"),
      @UniqueConstraint(
          name = "uq_payment_orders_provider_order_no",
          columnNames = "provider_order_no")
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentOrder extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "payment_order_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "payment_product_id", nullable = false)
  private PaymentProduct product;

  @Column(name = "order_key", nullable = false, length = 100)
  private String orderKey;

  @Column(name = "report_id", length = 100)
  private String reportId;

  @Column(name = "product_name", nullable = false, length = 100)
  private String productName;

  @Column(name = "amount", nullable = false)
  private long amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private PaymentOrderStatus status;

  @Column(name = "provider_order_no", length = 100)
  private String providerOrderNo;

  @Column(name = "requested_at")
  private OffsetDateTime requestedAt;

  @Builder
  private PaymentOrder(
      User user,
      PaymentProduct product,
      String orderKey,
      String reportId,
      String productName,
      long amount,
      PaymentOrderStatus status) {
    this.user = user;
    this.product = product;
    this.orderKey = orderKey;
    this.reportId = reportId;
    this.productName = productName;
    this.amount = amount;
    this.status = status;
  }

  public void markPaymentRequested(String providerOrderNo, OffsetDateTime requestedAt) {
    this.providerOrderNo = providerOrderNo;
    this.requestedAt = requestedAt;
    this.status = PaymentOrderStatus.PAYMENT_REQUESTED;
  }

  public void complete() {
    this.status = PaymentOrderStatus.COMPLETED;
  }

  public void cancel() {
    this.status = PaymentOrderStatus.CANCELED;
  }

  public void fail() {
    this.status = PaymentOrderStatus.FAILED;
  }
}
