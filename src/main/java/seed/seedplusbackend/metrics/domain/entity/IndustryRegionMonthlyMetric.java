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
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.global.common.BaseTimeEntity;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.region.domain.entity.Region;

@Getter
@Entity
@Table(
    name = "industry_region_monthly_metrics",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_industry_region_month",
          columnNames = {"region_id", "industry_id", "reference_month"})
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IndustryRegionMonthlyMetric extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "industry_region_monthly_metric_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "region_id", nullable = false)
  private Region region;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "industry_id", nullable = false)
  private Industry industry;

  @Column(name = "reference_month", nullable = false)
  private LocalDate referenceMonth;

  @Column(name = "average_sales", nullable = false)
  private long averageSales;

  @Column(name = "sales_change_rate", nullable = false, precision = 6, scale = 2)
  private BigDecimal salesChangeRate;

  @Column(name = "average_cost_rate", precision = 6, scale = 2)
  private BigDecimal averageCostRate;

  @Column(name = "closure_rate", precision = 6, scale = 2)
  private BigDecimal closureRate;

  @Column(name = "store_count", nullable = false)
  private long storeCount;

  @Column(name = "sales_rank")
  private Integer salesRank;

  @Column(name = "source_name", length = 100)
  private String sourceName;

  @Column(name = "collected_at")
  private OffsetDateTime collectedAt;

  @Builder
  private IndustryRegionMonthlyMetric(
      Region region,
      Industry industry,
      LocalDate referenceMonth,
      long averageSales,
      BigDecimal salesChangeRate,
      BigDecimal averageCostRate,
      BigDecimal closureRate,
      long storeCount,
      Integer salesRank,
      String sourceName,
      OffsetDateTime collectedAt) {
    this.region = region;
    this.industry = industry;
    this.referenceMonth = referenceMonth;
    this.averageSales = averageSales;
    this.salesChangeRate = salesChangeRate;
    this.averageCostRate = averageCostRate;
    this.closureRate = closureRate;
    this.storeCount = storeCount;
    this.salesRank = salesRank;
    this.sourceName = sourceName;
    this.collectedAt = collectedAt;
  }
}
