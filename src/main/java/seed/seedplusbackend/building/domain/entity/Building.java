package seed.seedplusbackend.building.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.locationtech.jts.geom.Point;
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.region.domain.entity.Region;

@Getter
@Entity
@Table(name = "buildings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Building {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "building_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "commercial_area_id", nullable = false)
  private CommercialArea commercialArea;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "region_id", nullable = false)
  private Region region;

  @Column(name = "address", length = 255)
  private String address;

  @Column(name = "name", length = 150)
  private String name;

  @Column(name = "total_floor")
  private Integer totalFloor;

  @Column(name = "total_area", precision = 12, scale = 2)
  private BigDecimal totalArea;

  @Column(name = "location", columnDefinition = "geography(Point,4326)")
  private Point location;

  @Builder
  private Building(
      CommercialArea commercialArea,
      Region region,
      String address,
      String name,
      Integer totalFloor,
      BigDecimal totalArea,
      Point location) {
    this.commercialArea = commercialArea;
    this.region = region;
    this.address = address;
    this.name = name;
    this.totalFloor = totalFloor;
    this.totalArea = totalArea;
    this.location = location;
  }
}
