package seed.seedplusbackend.commercial.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaType;
import seed.seedplusbackend.commercial.presentation.dto.CommercialAreaDetailResponse;
import seed.seedplusbackend.commercial.presentation.dto.CommercialAreaMetricResponse;
import seed.seedplusbackend.commercial.presentation.dto.CommercialAreaStatusFilter;
import seed.seedplusbackend.commercial.presentation.dto.PagedCommercialAreaResponse;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.swagger.annotation.ApiErrorCodeExamples;

@Tag(name = "상권", description = "상권 API")
public interface CommercialAreaApi {

  @Operation(summary = "상권 목록 조회", operationId = "getCommercialAreas")
  @ApiErrorCodeExamples({ErrorCode.INVALID_PARAMETER, ErrorCode.INVALID_SIZE})
  @GetMapping
  ResponseEntity<ApiResponse<PagedCommercialAreaResponse>> getCommercialAreas(
      @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
          @Min(0)
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "페이지 크기", example = "20")
          @Min(1)
          @Max(100)
          @RequestParam(defaultValue = "20")
          int size,
      @Parameter(description = "지역 ID 필터", example = "1") @RequestParam(required = false)
          Long regionId,
      @Parameter(description = "상권 유형 필터", example = "DEVELOPED") @RequestParam(required = false)
          CommercialAreaType type,
      @Parameter(description = "상권 상태 필터", example = "ACTIVE")
          @RequestParam(defaultValue = "ACTIVE")
          CommercialAreaStatusFilter status);

  @Operation(summary = "상권 상세 조회", operationId = "getCommercialArea")
  @ApiErrorCodeExamples({ErrorCode.NOT_FOUND_COMMERCIAL_AREA})
  @GetMapping("/{commercialAreaId}")
  ResponseEntity<ApiResponse<CommercialAreaDetailResponse>> getCommercialArea(
      @Parameter(description = "상권 ID", example = "1") @PathVariable Long commercialAreaId);

  @Operation(summary = "상권 월별 메트릭 조회", operationId = "getCommercialAreaMetrics")
  @ApiErrorCodeExamples({ErrorCode.INVALID_PARAMETER, ErrorCode.NOT_FOUND_COMMERCIAL_AREA})
  @GetMapping("/{commercialAreaId}/metrics")
  ResponseEntity<ApiResponse<List<CommercialAreaMetricResponse>>> getCommercialAreaMetrics(
      @Parameter(description = "상권 ID", example = "1") @PathVariable Long commercialAreaId,
      @Parameter(description = "시작 월 (yyyy-MM-01)", example = "2026-01-01")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          @RequestParam(required = false)
          LocalDate startMonth,
      @Parameter(description = "종료 월 (yyyy-MM-01)", example = "2026-12-01")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          @RequestParam(required = false)
          LocalDate endMonth);
}
