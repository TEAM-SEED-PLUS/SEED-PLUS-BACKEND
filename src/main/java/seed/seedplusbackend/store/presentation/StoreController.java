package seed.seedplusbackend.store.presentation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.metrics.presentation.dto.StoreFinancialMetricResponse;
import seed.seedplusbackend.metrics.presentation.dto.StoreOperationMetricResponse;
import seed.seedplusbackend.store.application.StoreQueryService;
import seed.seedplusbackend.store.presentation.dto.PagedStoreResponse;
import seed.seedplusbackend.store.presentation.dto.StoreDetailResponse;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stores")
public class StoreController implements StoreApi {

  private final StoreQueryService storeQueryService;

  @Override
  public ResponseEntity<ApiResponse<PagedStoreResponse>> getStores(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
      @RequestParam(required = false) @Positive Long buildingId,
      @RequestParam(required = false) @Positive Long industryId,
      @RequestParam(required = false) @Positive Long regionId,
      @RequestParam(required = false) Boolean isVacant) {
    return ResponseEntity.ok(
        ApiResponse.success(
            PagedStoreResponse.from(
                storeQueryService.getStores(
                    page, size, buildingId, industryId, regionId, isVacant))));
  }

  @Override
  public ResponseEntity<ApiResponse<StoreDetailResponse>> getStore(
      @PathVariable @Positive Long storeId) {
    return ResponseEntity.ok(
        ApiResponse.success(StoreDetailResponse.from(storeQueryService.getStore(storeId))));
  }

  @Override
  public ResponseEntity<ApiResponse<List<StoreOperationMetricResponse>>> getStoreOperationMetrics(
      @PathVariable @Positive Long storeId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startMonth,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endMonth) {
    return ResponseEntity.ok(
        ApiResponse.success(
            StoreOperationMetricResponse.from(
                storeQueryService.getStoreOperationMetrics(storeId, startMonth, endMonth))));
  }

  @Override
  public ResponseEntity<ApiResponse<List<StoreFinancialMetricResponse>>> getStoreFinancialMetrics(
      @PathVariable @Positive Long storeId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startMonth,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endMonth) {
    return ResponseEntity.ok(
        ApiResponse.success(
            StoreFinancialMetricResponse.from(
                storeQueryService.getStoreFinancialMetrics(storeId, startMonth, endMonth))));
  }
}
