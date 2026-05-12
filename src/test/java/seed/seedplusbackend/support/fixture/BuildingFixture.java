package seed.seedplusbackend.support.fixture;

import java.math.BigDecimal;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import seed.seedplusbackend.building.domain.entity.Building;
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.region.domain.entity.Region;

public final class BuildingFixture {

  private static final int WGS84_SRID = 4326;
  private static final GeometryFactory GEOMETRY_FACTORY =
      new GeometryFactory(new PrecisionModel(), WGS84_SRID);

  private BuildingFixture() {}

  public static Building seoulGangnamBuilding(Region region, CommercialArea commercialArea) {
    Point location = GEOMETRY_FACTORY.createPoint(new Coordinate(127.0364, 37.5012));
    location.setSRID(WGS84_SRID);
    return Building.builder()
        .commercialArea(commercialArea)
        .region(region)
        .address("서울 강남구 테헤란로 123")
        .name("테스트 빌딩")
        .totalFloor(15)
        .totalArea(new BigDecimal("12345.67"))
        .location(location)
        .build();
  }
}
