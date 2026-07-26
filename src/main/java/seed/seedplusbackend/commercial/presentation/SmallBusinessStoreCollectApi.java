package seed.seedplusbackend.commercial.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import seed.seedplusbackend.commercial.presentation.dto.SmallBusinessStoreCollectRequest;
import seed.seedplusbackend.commercial.presentation.dto.SmallBusinessStoreCollectResponse;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.swagger.annotation.ApiErrorCodeExamples;

@Tag(name = "상가정보 수집", description = "소상공인시장진흥공단 상권 내 상가정보 수집 API")
@SecurityRequirement(name = "bearerAuth")
public interface SmallBusinessStoreCollectApi {

  @Operation(summary = "상권 내 상가정보 수동 수집", operationId = "collectSmallBusinessStores")
  @ApiErrorCodeExamples({
    ErrorCode.INVALID_PARAMETER,
    ErrorCode.SMALL_BUSINESS_STORE_API_REQUEST_FAILED,
    ErrorCode.SMALL_BUSINESS_STORE_API_INVALID_RESPONSE,
    ErrorCode.COMMERCIAL_DATA_PROVIDER_FAILED
  })
  @PostMapping("/collect")
  ResponseEntity<ApiResponse<SmallBusinessStoreCollectResponse>> collect(
      @Valid @RequestBody SmallBusinessStoreCollectRequest request);
}
