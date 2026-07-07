package seed.seedplusbackend.commercial.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "commercial_estimated_sales")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommercialEstimatedSales {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "commercial_estimated_sales_id")
  private Long id;

  @Column(name = "stdr_yyqu_cd", nullable = false, length = 5)
  private String stdrYyquCd;

  @Column(name = "trdar_se_cd", length = 20)
  private String trdarSeCd;

  @Column(name = "trdar_se_cd_nm", length = 100)
  private String trdarSeCdNm;

  @Column(name = "trdar_cd", nullable = false, length = 30)
  private String trdarCd;

  @Column(name = "trdar_cd_nm", length = 150)
  private String trdarCdNm;

  @Column(name = "svc_induty_cd", nullable = false, length = 30)
  private String svcIndutyCd;

  @Column(name = "svc_induty_cd_nm", length = 100)
  private String svcIndutyCdNm;

  @Column(name = "thsmon_selng_amt", nullable = false)
  private Long thsmonSelngAmt = 0L;

  @Column(name = "thsmon_selng_co", nullable = false)
  private Long thsmonSelngCo = 0L;

  @Column(name = "collected_at", nullable = false)
  private OffsetDateTime collectedAt;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;
}
