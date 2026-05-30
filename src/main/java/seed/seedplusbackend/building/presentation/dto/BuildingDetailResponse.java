package seed.seedplusbackend.building.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.locationtech.jts.geom.Point;
import seed.seedplusbackend.building.application.result.BuildingDetailResult;
import seed.seedplusbackend.building.domain.entity.Building;
import seed.seedplusbackend.commercial.presentation.dto.CommercialAreaResponse;
import seed.seedplusbackend.region.presentation.dto.RegionResponse;

@Schema(description = "건물 상세 응답")
public record BuildingDetailResponse(
    @Schema(description = "건물 ID", example = "1") Long buildingId,
    @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123") String address,
    @Schema(description = "건물명", example = "테스트 빌딩") String name,
    @Schema(description = "층수", example = "5") Integer floor,
    @Schema(description = "총 면적", example = "120.5") BigDecimal totalArea,
    @Schema(description = "위도", example = "37.5001") Double latitude,
    @Schema(description = "경도", example = "127.0364") Double longitude,
    @Schema(description = "지역") RegionResponse region,
    @Schema(description = "상권") CommercialAreaResponse commercialArea,
    @Schema(description = "점포 수", example = "12") long storeCount,
    @Schema(description = "공실 점포 수", example = "3") long vacantStoreCount) {

  public static BuildingDetailResponse from(BuildingDetailResult result) {
    Building building = result.building();
    Point location = building.getLocation();
    return new BuildingDetailResponse(
        building.getId(),
        building.getAddress(),
        building.getName(),
        building.getTotalFloor(),
        building.getTotalArea(),
        latitude(location),
        longitude(location),
        RegionResponse.from(building.getRegion()),
        CommercialAreaResponse.from(building.getCommercialArea()),
        result.storeCount(),
        result.vacantStoreCount());
  }

  private static Double latitude(Point location) {
    return location == null ? null : location.getY();
  }

  private static Double longitude(Point location) {
    return location == null ? null : location.getX();
  }
}
