package seed.seedplusbackend.commercial.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import seed.seedplusbackend.commercial.presentation.dto.CommercialEstimatedSalesCollectRequest;
import seed.seedplusbackend.commercial.presentation.dto.CommercialEstimatedSalesCollectResponse;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.swagger.annotation.ApiErrorCodeExamples;

@Tag(name = "상권 추정매출 수집", description = "서울시 상권 추정매출 데이터 수집 API")
@SecurityRequirement(name = "bearerAuth")
public interface CommercialEstimatedSalesCollectApi {

  @Operation(summary = "서울시 상권 추정매출 수동 수집", operationId = "collectCommercialEstimatedSales")
  @ApiErrorCodeExamples({ErrorCode.INVALID_PARAMETER})
  @PostMapping("/collect")
  ResponseEntity<ApiResponse<CommercialEstimatedSalesCollectResponse>> collect(
      @Valid @RequestBody CommercialEstimatedSalesCollectRequest request);
}
