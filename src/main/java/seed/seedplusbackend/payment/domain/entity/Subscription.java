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
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.global.common.BaseTimeEntity;
import seed.seedplusbackend.user.domain.entity.User;

@Getter
@Entity
@Table(name = "subscriptions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "subscription_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "payment_product_id", nullable = false)
  private PaymentProduct product;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private SubscriptionStatus status;

  @Column(name = "encrypted_billing_key", length = 500)
  private String encryptedBillingKey;

  @Column(name = "started_at")
  private OffsetDateTime startedAt;

  @Column(name = "expires_at")
  private OffsetDateTime expiresAt;

  @Column(name = "next_billing_at")
  private OffsetDateTime nextBillingAt;

  @Column(name = "canceled_at")
  private OffsetDateTime canceledAt;

  @Builder
  private Subscription(
      User user,
      PaymentProduct product,
      SubscriptionStatus status,
      String encryptedBillingKey,
      OffsetDateTime startedAt,
      OffsetDateTime expiresAt,
      OffsetDateTime nextBillingAt) {
    this.user = user;
    this.product = product;
    this.status = status;
    this.encryptedBillingKey = encryptedBillingKey;
    this.startedAt = startedAt;
    this.expiresAt = expiresAt;
    this.nextBillingAt = nextBillingAt;
  }

  public boolean isActiveAt(OffsetDateTime now) {
    return status == SubscriptionStatus.ACTIVE && expiresAt != null && now.isBefore(expiresAt);
  }
}
