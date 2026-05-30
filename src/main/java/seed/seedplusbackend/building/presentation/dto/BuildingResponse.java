package seed.seedplusbackend.building.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.locationtech.jts.geom.Point;
import seed.seedplusbackend.building.domain.entity.Building;

@Schema(description = "건물 응답")
public record BuildingResponse(
    @Schema(description = "건물 ID", example = "1") Long buildingId,
    @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123") String address,
    @Schema(description = "건물명", example = "테스트 빌딩") String name,
    @Schema(description = "층수", example = "5") Integer floor,
    @Schema(description = "총 면적", example = "120.5") BigDecimal totalArea,
    @Schema(description = "위도", example = "37.5001") Double latitude,
    @Schema(description = "경도", example = "127.0364") Double longitude) {

  public static BuildingResponse from(Building building) {
    Point location = building.getLocation();
    return new BuildingResponse(
        building.getId(),
        building.getAddress(),
        building.getName(),
        building.getTotalFloor(),
        building.getTotalArea(),
        latitude(location),
        longitude(location));
  }

  private static Double latitude(Point location) {
    return location == null ? null : location.getY();
  }

  private static Double longitude(Point location) {
    return location == null ? null : location.getX();
  }
}
