package seed.seedplusbackend.commercial.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seed.seedplusbackend.region.domain.entity.Region;

@Getter
@Entity
@Table(
    name = "commercial_area_region_mappings",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_commercial_area_region",
          columnNames = {"commercial_area_id", "region_id"})
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommercialAreaRegionMapping {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "commercial_area_region_mapping_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "commercial_area_id", nullable = false)
  private CommercialArea commercialArea;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "region_id", nullable = false)
  private Region region;

  @Column(name = "is_primary", nullable = false)
  private boolean primary;

  @Builder
  private CommercialAreaRegionMapping(
      CommercialArea commercialArea, Region region, boolean primary) {
    this.commercialArea = commercialArea;
    this.region = region;
    this.primary = primary;
  }
}
