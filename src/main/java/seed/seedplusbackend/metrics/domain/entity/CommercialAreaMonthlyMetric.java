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
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.global.common.BaseTimeEntity;

@Getter
@Entity
@Table(
    name = "commercial_area_monthly_metrics",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_commercial_area_month",
          columnNames = {"commercial_area_id", "reference_month"})
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommercialAreaMonthlyMetric extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "commercial_area_monthly_metric_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "commercial_area_id", nullable = false)
  private CommercialArea commercialArea;

  @Column(name = "reference_month", nullable = false)
  private LocalDate referenceMonth;

  @Column(name = "floating_population", nullable = false)
  private long floatingPopulation;

  @Column(name = "vacancy_rate", nullable = false, precision = 6, scale = 2)
  private BigDecimal vacancyRate;

  @Column(name = "average_rent", nullable = false)
  private long averageRent;

  @Column(name = "opening_rate", nullable = false, precision = 6, scale = 2)
  private BigDecimal openingRate;

  @Column(name = "closure_rate", nullable = false, precision = 6, scale = 2)
  private BigDecimal closureRate;

  @Column(name = "sales_growth_rate", nullable = false, precision = 6, scale = 2)
  private BigDecimal salesGrowthRate;

  @Column(name = "competition_density", nullable = false, precision = 8, scale = 2)
  private BigDecimal competitionDensity;

  @Column(name = "activity_score", nullable = false)
  private int activityScore;

  @Column(name = "source_name", length = 100)
  private String sourceName;

  @Column(name = "collected_at")
  private OffsetDateTime collectedAt;

  @Builder
  private CommercialAreaMonthlyMetric(
      CommercialArea commercialArea,
      LocalDate referenceMonth,
      long floatingPopulation,
      BigDecimal vacancyRate,
      long averageRent,
      BigDecimal openingRate,
      BigDecimal closureRate,
      BigDecimal salesGrowthRate,
      BigDecimal competitionDensity,
      int activityScore,
      String sourceName,
      OffsetDateTime collectedAt) {
    this.commercialArea = commercialArea;
    this.referenceMonth = referenceMonth;
    this.floatingPopulation = floatingPopulation;
    this.vacancyRate = vacancyRate;
    this.averageRent = averageRent;
    this.openingRate = openingRate;
    this.closureRate = closureRate;
    this.salesGrowthRate = salesGrowthRate;
    this.competitionDensity = competitionDensity;
    this.activityScore = activityScore;
    this.sourceName = sourceName;
    this.collectedAt = collectedAt;
  }
}
