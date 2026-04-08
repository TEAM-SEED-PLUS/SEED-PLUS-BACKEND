package seed.seedplusbackend.domain.entity.home;

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
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.domain.entity.BaseEntity;
import seed.seedplusbackend.domain.entity.base.Industry;
import seed.seedplusbackend.domain.entity.base.Region;
import seed.seedplusbackend.domain.entity.home.enumerate.PeriodType;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndustryTrendMetric extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "region_id")
  private Region region;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "industry_id")
  private Industry industry;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PeriodType periodType; // 1M, 3M, 6M, 1Y

  @Column(nullable = false)
  private LocalDate pointDate;

  @Column(nullable = false)
  private Double salesAmount;
}
