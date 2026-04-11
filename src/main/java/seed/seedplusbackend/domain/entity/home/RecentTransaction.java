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
public class RecentTransaction extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "region_id")
  private Region region;

  @Column(nullable = false)
  private Double transactionPrice;

  @Column(nullable = false)
  private Double capRate;

  @Column(nullable = false)
  private LocalDate transactionDate;

  @Column(nullable = false)
  private String buildingName; // 상가, 근린상가 등

  @Column(nullable = false)
  private String address; // 전체 주소
}
