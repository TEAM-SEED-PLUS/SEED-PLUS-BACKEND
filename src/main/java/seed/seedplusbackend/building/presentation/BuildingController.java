package seed.seedplusbackend.building.presentation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import seed.seedplusbackend.building.application.BuildingQueryService;
import seed.seedplusbackend.building.application.query.BuildingSearchQuery;
import seed.seedplusbackend.building.presentation.dto.BuildingDetailResponse;
import seed.seedplusbackend.building.presentation.dto.BuildingResponse;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.response.PageResponse;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/buildings")
public class BuildingController implements BuildingApi {

  private final BuildingQueryService buildingQueryService;

  @Override
  public ResponseEntity<ApiResponse<PageResponse<BuildingResponse>>> getBuildings(
      @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
      @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size,
      @RequestParam(name = "regionId", required = false) @Positive Long regionId,
      @RequestParam(name = "commercialAreaId", required = false) @Positive Long commercialAreaId) {
    return ResponseEntity.ok(
        ApiResponse.success(
            PageResponse.from(
                buildingQueryService.getBuildings(
                    new BuildingSearchQuery(page, size, regionId, commercialAreaId)),
                BuildingResponse::from)));
  }

  @Override
  public ResponseEntity<ApiResponse<BuildingDetailResponse>> getBuilding(
      @PathVariable(name = "buildingId") @Positive Long buildingId) {
    return ResponseEntity.ok(
        ApiResponse.success(
            BuildingDetailResponse.from(buildingQueryService.getBuilding(buildingId))));
  }
}
