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
import seed.seedplusbackend.domain.entity.base.Industry;
import seed.seedplusbackend.domain.entity.base.Region;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndustryRegionMetric {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "region_id")
  private Region region;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "industry_id")
  private Industry industry;

  @Column(nullable = false)
  private LocalDate referenceDate;

  @Column(nullable = false)
  private Double averageSales;

  @Column(nullable = false)
  private Double salesChangeRate;

  @Column(nullable = false)
  private Double floatingPopulation;

  @Column(nullable = false)
  private Double costRate;

  @Column(nullable = false)
  private Integer rank;
}
