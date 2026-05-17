package seed.seedplusbackend.building.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import seed.seedplusbackend.building.presentation.dto.BuildingDetailResponse;
import seed.seedplusbackend.building.presentation.dto.BuildingResponse;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.response.PageResponse;
import seed.seedplusbackend.global.swagger.annotation.ApiErrorCodeExamples;

@Tag(name = "Buildings", description = "건물 API")
public interface BuildingApi {

  @Operation(summary = "건물 목록 조회", operationId = "getBuildings")
  @GetMapping
  ResponseEntity<ApiResponse<PageResponse<BuildingResponse>>> getBuildings(
      @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
          @RequestParam(name = "page", defaultValue = "0")
          @Min(0)
          int page,
      @Parameter(description = "페이지 크기", example = "20")
          @RequestParam(name = "size", defaultValue = "20")
          @Min(1)
          @Max(100)
          int size,
      @Parameter(description = "지역 ID", example = "1")
          @RequestParam(name = "regionId", required = false)
          Long regionId,
      @Parameter(description = "상권 ID", example = "1")
          @RequestParam(name = "commercialAreaId", required = false)
          Long commercialAreaId);

  @Operation(summary = "건물 상세 조회", operationId = "getBuilding")
  @ApiErrorCodeExamples({ErrorCode.NOT_FOUND_BUILDING})
  @GetMapping("/{buildingId}")
  ResponseEntity<ApiResponse<BuildingDetailResponse>> getBuilding(
      @Parameter(description = "건물 ID", example = "1") @PathVariable(name = "buildingId")
          Long buildingId);
}
