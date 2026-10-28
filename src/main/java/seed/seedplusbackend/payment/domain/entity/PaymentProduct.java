package seed.seedplusbackend.payment.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.global.common.BaseTimeEntity;

@Getter
@Entity
@Table(
    name = "payment_products",
    uniqueConstraints = {
      @UniqueConstraint(name = "uq_payment_products_code", columnNames = "product_code")
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentProduct extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "payment_product_id")
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "product_code", nullable = false, length = 30)
  private PaymentProductCode code;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "billing_type", nullable = false, length = 20)
  private PaymentProductBillingType billingType;

  @Column(name = "amount", nullable = false)
  private long amount;

  @Column(name = "active", nullable = false)
  private boolean active;

  @Builder
  private PaymentProduct(
      PaymentProductCode code,
      String name,
      PaymentProductBillingType billingType,
      long amount,
      boolean active) {
    this.code = code;
    this.name = name;
    this.billingType = billingType;
    this.amount = amount;
    this.active = active;
  }

  public void changeAmount(long amount) {
    this.amount = amount;
  }

  public void deactivate() {
    this.active = false;
  }
}
