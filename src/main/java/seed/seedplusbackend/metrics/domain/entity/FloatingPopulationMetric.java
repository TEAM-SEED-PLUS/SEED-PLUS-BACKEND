package seed.seedplusbackend.metrics.domain.entity;

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
import java.time.LocalDate;
import java.time.LocalTime;
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
@Table(name = "floating_population_metrics")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FloatingPopulationMetric extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "floating_population_metric_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "region_id")
  private Region region;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "commercial_area_id")
  private CommercialArea commercialArea;

  @Column(name = "reference_date", nullable = false)
  private LocalDate referenceDate;

  @Column(name = "time_slot_start", nullable = false)
  private LocalTime timeSlotStart;

  @Column(name = "age_group", nullable = false, length = 20)
  private String ageGroup;

  @Enumerated(EnumType.STRING)
  @Column(name = "gender", nullable = false, length = 10)
  private FloatingPopulationGender gender;

  @Column(name = "population_count", nullable = false)
  private long populationCount;

  @Column(name = "source_name", length = 100)
  private String sourceName;

  @Column(name = "collected_at")
  private OffsetDateTime collectedAt;

  @Builder
  private FloatingPopulationMetric(
      Region region,
      CommercialArea commercialArea,
      LocalDate referenceDate,
      LocalTime timeSlotStart,
      String ageGroup,
      FloatingPopulationGender gender,
      long populationCount,
      String sourceName,
      OffsetDateTime collectedAt) {
    this.region = region;
    this.commercialArea = commercialArea;
    this.referenceDate = referenceDate;
    this.timeSlotStart = timeSlotStart;
    this.ageGroup = ageGroup;
    this.gender = gender;
    this.populationCount = populationCount;
    this.sourceName = sourceName;
    this.collectedAt = collectedAt;
  }
}
