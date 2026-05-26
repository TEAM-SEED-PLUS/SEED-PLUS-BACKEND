package seed.seedplusbackend.building.application;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.building.application.command.CreateBuildingCommand;
import seed.seedplusbackend.building.domain.entity.Building;
import seed.seedplusbackend.building.domain.repository.BuildingRepository;
import seed.seedplusbackend.commercial.domain.entity.CommercialArea;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaStatus;
import seed.seedplusbackend.commercial.domain.repository.CommercialAreaRepository;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.domain.repository.RegionRepository;

@Service
@RequiredArgsConstructor
public class BuildingCommandService {

  private static final int WGS84_SRID = 4326;
  private static final double SAME_BUILDING_DISTANCE_METERS = 5.0;
  private static final GeometryFactory GEOMETRY_FACTORY =
      new GeometryFactory(new PrecisionModel(), WGS84_SRID);

  private final BuildingRepository buildingRepository;
  private final RegionRepository regionRepository;
  private final CommercialAreaRepository commercialAreaRepository;

  @Transactional
  public Building create(CreateBuildingCommand command) {
    return resolveOrCreate(command);
  }

  @Transactional
  public Building resolveOrCreate(CreateBuildingCommand command) {
    Region region =
        regionRepository
            .findById(command.regionId())
            .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_REGION));
    CommercialArea commercialArea =
        commercialAreaRepository
            .findByIdAndStatusNot(command.commercialAreaId(), CommercialAreaStatus.DELETED)
            .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_COMMERCIAL_AREA));

    return resolveOrCreate(command, region, commercialArea);
  }

  @Transactional
  public Building resolveOrCreate(
      CreateBuildingCommand command, Region region, CommercialArea commercialArea) {
    Point location = toPoint(command.latitude(), command.longitude());
    return buildingRepository
        .findFirstByRegion_IdAndCommercialArea_IdAndAddressOrderByIdAsc(
            command.regionId(), command.commercialAreaId(), command.address())
        .or(
            () ->
                location == null
                    ? java.util.Optional.empty()
                    : buildingRepository.findNearestWithinDistance(
                        command.regionId(),
                        command.commercialAreaId(),
                        command.latitude(),
                        command.longitude(),
                        SAME_BUILDING_DISTANCE_METERS))
        .orElseGet(() -> save(command, region, commercialArea, location));
  }

  private Building save(
      CreateBuildingCommand command, Region region, CommercialArea commercialArea, Point location) {
    return buildingRepository.save(
        Building.builder()
            .region(region)
            .commercialArea(commercialArea)
            .address(command.address())
            .name(command.name())
            .totalFloor(command.totalFloor())
            .totalArea(command.totalArea())
            .location(location)
            .build());
  }

  private Point toPoint(BigDecimal latitude, BigDecimal longitude) {
    if (latitude == null && longitude == null) {
      return null;
    }
    if (latitude == null || longitude == null) {
      throw new ApplicationException(ErrorCode.INVALID_PARAMETER);
    }

    Point point =
        GEOMETRY_FACTORY.createPoint(
            new Coordinate(longitude.doubleValue(), latitude.doubleValue()));
    point.setSRID(WGS84_SRID);
    return point;
  }
}
