package seed.seedplusbackend.commercial.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import seed.seedplusbackend.commercial.presentation.dto.SeoulSdotFootTrafficCollectRequest;
import seed.seedplusbackend.commercial.presentation.dto.SeoulSdotFootTrafficCollectResponse;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.swagger.annotation.ApiErrorCodeExamples;

@Tag(name = "S-DoT 유동인구 수집", description = "서울시 S-DoT 센서 유동인구 데이터 수집 API")
@SecurityRequirement(name = "bearerAuth")
public interface SeoulSdotFootTrafficCollectApi {

  @Operation(summary = "서울시 S-DoT 유동인구 수동 수집", operationId = "collectSeoulSdotFootTraffic")
  @ApiErrorCodeExamples({ErrorCode.INVALID_PARAMETER})
  @PostMapping("/collect")
  ResponseEntity<ApiResponse<SeoulSdotFootTrafficCollectResponse>> collect(
      @Valid @RequestBody SeoulSdotFootTrafficCollectRequest request);
}
