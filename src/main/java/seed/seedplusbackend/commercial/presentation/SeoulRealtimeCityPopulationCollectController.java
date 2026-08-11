package seed.seedplusbackend.commercial.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seed.seedplusbackend.commercial.application.CommercialDataCollectService;
import seed.seedplusbackend.commercial.application.result.CommercialDataCollectResult;
import seed.seedplusbackend.commercial.presentation.dto.SeoulRealtimeCityPopulationCollectRequest;
import seed.seedplusbackend.commercial.presentation.dto.SeoulRealtimeCityPopulationCollectResponse;
import seed.seedplusbackend.global.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seoul-realtime-city-populations")
public class SeoulRealtimeCityPopulationCollectController
    implements SeoulRealtimeCityPopulationCollectApi {
  private final CommercialDataCollectService commercialDataCollectService;

  @Override
  public ResponseEntity<ApiResponse<SeoulRealtimeCityPopulationCollectResponse>> collect(
      @Valid @RequestBody SeoulRealtimeCityPopulationCollectRequest request) {
    CommercialDataCollectResult result = commercialDataCollectService.collect(request.toCommand());
    return ResponseEntity.ok(
        ApiResponse.success(SeoulRealtimeCityPopulationCollectResponse.from(result)));
  }
}
