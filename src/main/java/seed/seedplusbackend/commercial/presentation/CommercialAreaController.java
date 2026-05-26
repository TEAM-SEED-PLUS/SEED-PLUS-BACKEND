package seed.seedplusbackend.commercial.presentation;

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
import seed.seedplusbackend.commercial.application.CommercialAreaQueryService;
import seed.seedplusbackend.commercial.application.query.CommercialAreaSearchQuery;
import seed.seedplusbackend.commercial.domain.entity.CommercialAreaType;
import seed.seedplusbackend.commercial.presentation.dto.CommercialAreaDetailResponse;
import seed.seedplusbackend.commercial.presentation.dto.CommercialAreaMetricResponse;
import seed.seedplusbackend.commercial.presentation.dto.CommercialAreaStatusFilter;
import seed.seedplusbackend.commercial.presentation.dto.PagedCommercialAreaResponse;
import seed.seedplusbackend.global.response.ApiResponse;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/commercial-areas")
public class CommercialAreaController implements CommercialAreaApi {

  private final CommercialAreaQueryService commercialAreaQueryService;

  @Override
  public ResponseEntity<ApiResponse<PagedCommercialAreaResponse>> getCommercialAreas(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
      @RequestParam(required = false) @Positive Long regionId,
      @RequestParam(required = false) CommercialAreaType type,
      @RequestParam(defaultValue = "ACTIVE") CommercialAreaStatusFilter status) {
    CommercialAreaStatusFilter resolvedStatus =
        status == null ? CommercialAreaStatusFilter.ACTIVE : status;
    CommercialAreaSearchQuery query =
        new CommercialAreaSearchQuery(page, size, regionId, type, resolvedStatus.toDomainStatus());

    return ResponseEntity.ok(
        ApiResponse.success(
            PagedCommercialAreaResponse.from(
                commercialAreaQueryService.getCommercialAreas(query))));
  }

  @Override
  public ResponseEntity<ApiResponse<CommercialAreaDetailResponse>> getCommercialArea(
      @PathVariable @Positive Long commercialAreaId) {
    return ResponseEntity.ok(
        ApiResponse.success(
            CommercialAreaDetailResponse.from(
                commercialAreaQueryService.getCommercialArea(commercialAreaId))));
  }

  @Override
  public ResponseEntity<ApiResponse<List<CommercialAreaMetricResponse>>> getCommercialAreaMetrics(
      @PathVariable @Positive Long commercialAreaId,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(required = false)
          LocalDate startMonth,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(required = false)
          LocalDate endMonth) {
    return ResponseEntity.ok(
        ApiResponse.success(
            CommercialAreaMetricResponse.from(
                commercialAreaQueryService.getCommercialAreaMetrics(
                    commercialAreaId, startMonth, endMonth))));
  }
}
