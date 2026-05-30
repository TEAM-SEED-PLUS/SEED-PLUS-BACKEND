package seed.seedplusbackend.metrics.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.global.common.BaseTimeEntity;
import seed.seedplusbackend.store.domain.entity.Store;

@Getter
@Entity
@Table(
    name = "store_operation_monthly_metrics",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_store_operation_month",
          columnNames = {"store_id", "reference_month"})
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreOperationMonthlyMetric extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "store_operation_monthly_metric_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "store_id", nullable = false)
  private Store store;

  @Column(name = "reference_month", nullable = false)
  private LocalDate referenceMonth;

  @Column(name = "monthly_sales", nullable = false)
  private long monthlySales;

  @Column(name = "average_daily_sales", nullable = false)
  private long averageDailySales;

  @Column(name = "customer_count", nullable = false)
  private long customerCount;

  @Column(name = "average_order_amount", nullable = false)
  private long averageOrderAmount;

  @Column(name = "revisit_rate", nullable = false, precision = 6, scale = 2)
  private BigDecimal revisitRate;

  @Column(name = "table_turnover_rate", nullable = false, precision = 6, scale = 2)
  private BigDecimal tableTurnoverRate;

  @Column(name = "delivery_sales_ratio", nullable = false, precision = 6, scale = 2)
  private BigDecimal deliverySalesRatio;

  @Builder
  private StoreOperationMonthlyMetric(
      Store store,
      LocalDate referenceMonth,
      long monthlySales,
      long averageDailySales,
      long customerCount,
      long averageOrderAmount,
      BigDecimal revisitRate,
      BigDecimal tableTurnoverRate,
      BigDecimal deliverySalesRatio) {
    this.store = store;
    this.referenceMonth = referenceMonth;
    this.monthlySales = monthlySales;
    this.averageDailySales = averageDailySales;
    this.customerCount = customerCount;
    this.averageOrderAmount = averageOrderAmount;
    this.revisitRate = revisitRate;
    this.tableTurnoverRate = tableTurnoverRate;
    this.deliverySalesRatio = deliverySalesRatio;
  }
}
