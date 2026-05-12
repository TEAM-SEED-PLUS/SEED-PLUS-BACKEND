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
    name = "store_financial_monthly_metrics",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_store_financial_month",
          columnNames = {"store_id", "reference_month"})
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreFinancialMonthlyMetric extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "store_financial_monthly_metric_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "store_id", nullable = false)
  private Store store;

  @Column(name = "reference_month", nullable = false)
  private LocalDate referenceMonth;

  @Column(name = "rent_amount", nullable = false)
  private long rentAmount;

  @Column(name = "labor_cost", nullable = false)
  private long laborCost;

  @Column(name = "material_cost", nullable = false)
  private long materialCost;

  @Column(name = "fixed_cost", nullable = false)
  private long fixedCost;

  @Column(name = "variable_cost", nullable = false)
  private long variableCost;

  @Column(name = "cost_rate", nullable = false, precision = 6, scale = 2)
  private BigDecimal costRate;

  @Column(name = "operating_profit", nullable = false)
  private long operatingProfit;

  @Column(name = "operating_profit_rate", precision = 6, scale = 2)
  private BigDecimal operatingProfitRate;

  @Column(name = "cash_flow", nullable = false)
  private long cashFlow;

  @Builder
  private StoreFinancialMonthlyMetric(
      Store store,
      LocalDate referenceMonth,
      long rentAmount,
      long laborCost,
      long materialCost,
      long fixedCost,
      long variableCost,
      BigDecimal costRate,
      long operatingProfit,
      BigDecimal operatingProfitRate,
      long cashFlow) {
    this.store = store;
    this.referenceMonth = referenceMonth;
    this.rentAmount = rentAmount;
    this.laborCost = laborCost;
    this.materialCost = materialCost;
    this.fixedCost = fixedCost;
    this.variableCost = variableCost;
    this.costRate = costRate;
    this.operatingProfit = operatingProfit;
    this.operatingProfitRate = operatingProfitRate;
    this.cashFlow = cashFlow;
  }
}
