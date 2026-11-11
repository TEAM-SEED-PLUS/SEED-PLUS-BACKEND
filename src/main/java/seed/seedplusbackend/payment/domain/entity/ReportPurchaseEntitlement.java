package seed.seedplusbackend.payment.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import seed.seedplusbackend.global.common.BaseCreatedEntity;
import seed.seedplusbackend.user.domain.entity.User;

@Getter
@Entity
@Table(
    name = "report_purchase_entitlements",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_report_purchase_entitlements_user_report",
          columnNames = {"user_id", "report_id"}),
      @UniqueConstraint(
          name = "uq_report_purchase_entitlements_payment_order",
          columnNames = "payment_order_id")
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportPurchaseEntitlement extends BaseCreatedEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "report_purchase_entitlement_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "report_id", nullable = false, length = 100)
  private String reportId;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "payment_order_id", nullable = false)
  private PaymentOrder paymentOrder;

  @Column(name = "granted_at", nullable = false)
  private OffsetDateTime grantedAt;

  @Builder
  private ReportPurchaseEntitlement(
      User user, String reportId, PaymentOrder paymentOrder, OffsetDateTime grantedAt) {
    this.user = user;
    this.reportId = reportId;
    this.paymentOrder = paymentOrder;
    this.grantedAt = grantedAt;
  }

  public boolean grantsAccessTo(String reportId) {
    return this.reportId.equals(reportId);
  }
}
