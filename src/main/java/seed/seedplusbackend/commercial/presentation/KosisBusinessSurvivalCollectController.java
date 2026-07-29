package seed.seedplusbackend.commercial.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seed.seedplusbackend.commercial.application.CommercialDataCollectService;
import seed.seedplusbackend.commercial.application.result.CommercialDataCollectResult;
import seed.seedplusbackend.commercial.presentation.dto.KosisBusinessSurvivalCollectRequest;
import seed.seedplusbackend.commercial.presentation.dto.KosisBusinessSurvivalCollectResponse;
import seed.seedplusbackend.global.response.ApiResponse;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/kosis-business-survival-rates")
public class KosisBusinessSurvivalCollectController implements KosisBusinessSurvivalCollectApi {

  private final CommercialDataCollectService commercialDataCollectService;

  @Override
  @PostMapping("/collect")
  public ResponseEntity<ApiResponse<KosisBusinessSurvivalCollectResponse>> collect(
      @Valid @RequestBody KosisBusinessSurvivalCollectRequest request) {
    CommercialDataCollectResult result = commercialDataCollectService.collect(request.toCommand());
    return ResponseEntity.ok(
        ApiResponse.success(KosisBusinessSurvivalCollectResponse.from(result)));
  }
}
