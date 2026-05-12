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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "score_component_snapshots")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScoreComponentSnapshot {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "score_component_snapshot_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "score_snapshot_id", nullable = false)
  private ScoreSnapshot scoreSnapshot;

  @Enumerated(EnumType.STRING)
  @Column(name = "component_type", nullable = false, length = 30)
  private ScoreComponentType componentType;

  @Column(name = "raw_value", precision = 18, scale = 4)
  private BigDecimal rawValue;

  @Column(name = "normalized_score", precision = 6, scale = 2)
  private BigDecimal normalizedScore;

  @Column(name = "weight", precision = 6, scale = 4)
  private BigDecimal weight;

  @Column(name = "contribution", precision = 8, scale = 4)
  private BigDecimal contribution;

  @Builder
  private ScoreComponentSnapshot(
      ScoreSnapshot scoreSnapshot,
      ScoreComponentType componentType,
      BigDecimal rawValue,
      BigDecimal normalizedScore,
      BigDecimal weight,
      BigDecimal contribution) {
    this.scoreSnapshot = scoreSnapshot;
    this.componentType = componentType;
    this.rawValue = rawValue;
    this.normalizedScore = normalizedScore;
    this.weight = weight;
    this.contribution = contribution;
  }
}
