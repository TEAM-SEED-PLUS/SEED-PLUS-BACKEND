package seed.seedplusbackend.commercial.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import seed.seedplusbackend.commercial.presentation.dto.SeoulRealtimeCityPopulationCollectRequest;
import seed.seedplusbackend.commercial.presentation.dto.SeoulRealtimeCityPopulationCollectResponse;
import seed.seedplusbackend.global.response.ApiResponse;

@Tag(name = "서울시 실시간 도시 인구 수집", description = "서울시 주요 장소 실시간 인구 수집 API")
@SecurityRequirement(name = "bearerAuth")
public interface SeoulRealtimeCityPopulationCollectApi {
  @Operation(summary = "서울시 장소별 실시간 인구 수동 수집", operationId = "collectRealtimeCityPopulation")
  @PostMapping("/collect")
  ResponseEntity<ApiResponse<SeoulRealtimeCityPopulationCollectResponse>> collect(
      @Valid @RequestBody SeoulRealtimeCityPopulationCollectRequest request);
}
