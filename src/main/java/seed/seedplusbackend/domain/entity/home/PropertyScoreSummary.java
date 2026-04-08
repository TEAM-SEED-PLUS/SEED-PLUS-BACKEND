package seed.seedplusbackend.domain.entity.home;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.domain.entity.BaseEntity;
import seed.seedplusbackend.domain.entity.base.Region;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyScoreSummary extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "region_id")
  private Region region;

  @Column(nullable = false)
  private LocalDate referenceDate;

  @Column(nullable = false)
  private Double incomeScore;

  @Column(nullable = false)
  private Double riskScore;

  @Column(nullable = false)
  private Double liquidityScore;

  @Column(nullable = false)
  private Double totalScore;

  @Column(nullable = false)
  private Integer percentageRank;

  @Column(nullable = false)
  private String grade;
}
