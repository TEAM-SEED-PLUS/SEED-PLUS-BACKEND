package seed.seedplusbackend.store.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.swagger.annotation.ApiErrorCodeExamples;
import seed.seedplusbackend.metrics.presentation.dto.StoreFinancialMetricResponse;
import seed.seedplusbackend.metrics.presentation.dto.StoreOperationMetricResponse;
import seed.seedplusbackend.store.presentation.dto.PagedStoreResponse;
import seed.seedplusbackend.store.presentation.dto.StoreDetailResponse;

@Tag(name = "점포", description = "점포 API")
public interface StoreApi {

  @Operation(summary = "실제 점포 목록 조회", operationId = "getStores")
  @ApiErrorCodeExamples({ErrorCode.INVALID_PARAMETER})
  @GetMapping
  ResponseEntity<ApiResponse<PagedStoreResponse>> getStores(
      @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") @Min(0)
          int page,
      @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") @Min(1) @Max(100)
          int size,
      @Parameter(description = "건물 ID") @RequestParam(required = false) @Positive Long buildingId,
      @Parameter(description = "업종 ID") @RequestParam(required = false) @Positive Long industryId,
      @Parameter(description = "지역 ID") @RequestParam(required = false) @Positive Long regionId,
      @Parameter(description = "공실 여부") @RequestParam(required = false) Boolean isVacant);

  @Operation(summary = "점포 상세 조회", operationId = "getStore")
  @ApiErrorCodeExamples({ErrorCode.NOT_FOUND_STORE})
  @GetMapping("/{storeId}")
  ResponseEntity<ApiResponse<StoreDetailResponse>> getStore(
      @Parameter(description = "점포 ID", example = "1") @PathVariable @Positive Long storeId);

  @Operation(
      summary = "점포 운영 메트릭 조회",
      operationId = "getStoreOperationMetrics",
      tags = {"점포", "메트릭"})
  @ApiErrorCodeExamples({ErrorCode.INVALID_PARAMETER, ErrorCode.NOT_FOUND_STORE})
  @GetMapping("/{storeId}/operation-metrics")
  ResponseEntity<ApiResponse<List<StoreOperationMetricResponse>>> getStoreOperationMetrics(
      @Parameter(description = "점포 ID", example = "1") @PathVariable @Positive Long storeId,
      @Parameter(description = "시작 월", example = "2026-01-01")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startMonth,
      @Parameter(description = "종료 월", example = "2026-05-01")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endMonth);

  @Operation(
      summary = "점포 재무 메트릭 조회",
      operationId = "getStoreFinancialMetrics",
      tags = {"점포", "메트릭"})
  @ApiErrorCodeExamples({ErrorCode.INVALID_PARAMETER, ErrorCode.NOT_FOUND_STORE})
  @GetMapping("/{storeId}/financial-metrics")
  ResponseEntity<ApiResponse<List<StoreFinancialMetricResponse>>> getStoreFinancialMetrics(
      @Parameter(description = "점포 ID", example = "1") @PathVariable @Positive Long storeId,
      @Parameter(description = "시작 월", example = "2026-01-01")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startMonth,
      @Parameter(description = "종료 월", example = "2026-05-01")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endMonth);
}
