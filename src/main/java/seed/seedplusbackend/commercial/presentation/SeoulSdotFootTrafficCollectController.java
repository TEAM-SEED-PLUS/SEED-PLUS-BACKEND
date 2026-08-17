package seed.seedplusbackend.commercial.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seed.seedplusbackend.commercial.application.CommercialDataCollectService;
import seed.seedplusbackend.commercial.application.result.CommercialDataCollectResult;
import seed.seedplusbackend.commercial.presentation.dto.SeoulSdotFootTrafficCollectRequest;
import seed.seedplusbackend.commercial.presentation.dto.SeoulSdotFootTrafficCollectResponse;
import seed.seedplusbackend.global.response.ApiResponse;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seoul-sdot-foot-traffic")
public class SeoulSdotFootTrafficCollectController implements SeoulSdotFootTrafficCollectApi {

  private final CommercialDataCollectService commercialDataCollectService;

  @Override
  public ResponseEntity<ApiResponse<SeoulSdotFootTrafficCollectResponse>> collect(
      @Valid @RequestBody SeoulSdotFootTrafficCollectRequest request) {
    CommercialDataCollectResult result = commercialDataCollectService.collect(request.toCommand());
    return ResponseEntity.ok(ApiResponse.success(SeoulSdotFootTrafficCollectResponse.from(result)));
  }
}
