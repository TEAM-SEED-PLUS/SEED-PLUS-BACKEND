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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.global.common.BaseTimeEntity;
import seed.seedplusbackend.region.domain.entity.Region;

@Getter
@Entity
@Table(name = "rent_market_monthly_metrics")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RentMarketMonthlyMetric extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "rent_market_monthly_metric_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "region_id")
  private Region region;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "commercial_area_id")
  private CommercialArea commercialArea;

  @Column(name = "reference_month", nullable = false)
  private LocalDate referenceMonth;

  @Column(name = "average_rent_per_area")
  private Long averageRentPerArea;

  @Column(name = "average_deposit")
  private Long averageDeposit;

  @Column(name = "vacancy_rate", precision = 6, scale = 2)
  private BigDecimal vacancyRate;

  @Column(name = "rent_change_rate", precision = 6, scale = 2)
  private BigDecimal rentChangeRate;

  @Column(name = "source_name", length = 100)
  private String sourceName;

  @Column(name = "collected_at")
  private OffsetDateTime collectedAt;

  @Builder
  private RentMarketMonthlyMetric(
      Region region,
      CommercialArea commercialArea,
      LocalDate referenceMonth,
      Long averageRentPerArea,
      Long averageDeposit,
      BigDecimal vacancyRate,
      BigDecimal rentChangeRate,
      String sourceName,
      OffsetDateTime collectedAt) {
    this.region = region;
    this.commercialArea = commercialArea;
    this.referenceMonth = referenceMonth;
    this.averageRentPerArea = averageRentPerArea;
    this.averageDeposit = averageDeposit;
    this.vacancyRate = vacancyRate;
    this.rentChangeRate = rentChangeRate;
    this.sourceName = sourceName;
    this.collectedAt = collectedAt;
  }
}
