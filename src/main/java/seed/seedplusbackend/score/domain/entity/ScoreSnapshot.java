package seed.seedplusbackend.score.domain.entity;

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
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.builderstore.domain.entity.BuilderStore;
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.global.common.BaseCreatedEntity;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.store.domain.entity.Store;

@Getter
@Entity
@Table(name = "score_snapshots")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScoreSnapshot extends BaseCreatedEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "score_snapshot_id")
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false, length = 30)
  private ScoreTargetType targetType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "store_id")
  private Store store;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "builder_store_id")
  private BuilderStore builderStore;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "commercial_area_id")
  private CommercialArea commercialArea;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "region_id")
  private Region region;

  @Enumerated(EnumType.STRING)
  @Column(name = "score_type", nullable = false, length = 20)
  private ScoreType scoreType;

  @Column(name = "reference_month", nullable = false)
  private LocalDate referenceMonth;

  @Column(name = "total_score", nullable = false, precision = 5, scale = 2)
  private BigDecimal totalScore;

  @Enumerated(EnumType.STRING)
  @Column(name = "grade", nullable = false, length = 5)
  private ScoreGrade grade;

  @Column(name = "pd", precision = 6, scale = 2)
  private BigDecimal pd;

  @Column(name = "survival_probability", precision = 6, scale = 2)
  private BigDecimal survivalProbability;

  @Builder
  private ScoreSnapshot(
      ScoreTargetType targetType,
      Store store,
      BuilderStore builderStore,
      CommercialArea commercialArea,
      Region region,
      ScoreType scoreType,
      LocalDate referenceMonth,
      BigDecimal totalScore,
      ScoreGrade grade,
      BigDecimal pd,
      BigDecimal survivalProbability) {
    this.targetType = targetType;
    this.store = store;
    this.builderStore = builderStore;
    this.commercialArea = commercialArea;
    this.region = region;
    this.scoreType = scoreType;
    this.referenceMonth = referenceMonth;
    this.totalScore = totalScore;
    this.grade = grade;
    this.pd = pd;
    this.survivalProbability = survivalProbability;
  }
}
