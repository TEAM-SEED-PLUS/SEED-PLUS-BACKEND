package seed.seedplusbackend.region.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "regions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Region {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "region_id")
  private Long id;

  @Column(name = "sido", nullable = false, length = 50)
  private String sido;

  @Column(name = "sigungu", nullable = false, length = 50)
  private String sigungu;

  @Column(name = "dong", nullable = false, length = 80)
  private String dong;

  @Column(name = "code", length = 30)
  private String code;

  @Enumerated(EnumType.STRING)
  @Column(name = "code_type", nullable = false, length = 20)
  private RegionCodeType codeType;

  @Builder
  private Region(String sido, String sigungu, String dong, String code, RegionCodeType codeType) {
    this.sido = sido;
    this.sigungu = sigungu;
    this.dong = dong;
    this.code = code;
    this.codeType = codeType;
  }
}
