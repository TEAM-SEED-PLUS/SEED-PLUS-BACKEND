package seed.seedplusbackend.commercial.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "region_external_code_mappings",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_region_external_code_mapping",
          columnNames = {"region_code", "source", "external_code"})
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionExternalCodeMapping {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "region_external_code_mapping_id")
  private Long id;

  @Column(name = "region_code", nullable = false, length = 30)
  private String regionCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "source", nullable = false, length = 50)
  private ExternalDataSource source;

  @Column(name = "external_code", nullable = false, length = 100)
  private String externalCode;

  @Column(name = "external_name", length = 200)
  private String externalName;

  @Builder
  private RegionExternalCodeMapping(
      String regionCode, ExternalDataSource source, String externalCode, String externalName) {
    this.regionCode = regionCode;
    this.source = source;
    this.externalCode = externalCode;
    this.externalName = externalName;
  }
}
