package seed.seedplusbackend.domain.entity.home;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.domain.entity.home.enumerate.IndicatorType;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketIndicator {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  private IndicatorType indicatorType;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private Double value;

  @Column(nullable = false)
  private String unit;

  @Column(nullable = false)
  private Double changeValue;

  @Column(nullable = false)
  private Double changeRate;

  @Column(nullable = false)
  private LocalDate referenceDate;
}
