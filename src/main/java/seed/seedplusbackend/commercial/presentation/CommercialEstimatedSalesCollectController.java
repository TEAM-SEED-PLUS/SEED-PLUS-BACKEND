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
import seed.seedplusbackend.commercial.presentation.dto.CommercialEstimatedSalesCollectRequest;
import seed.seedplusbackend.commercial.presentation.dto.CommercialEstimatedSalesCollectResponse;
import seed.seedplusbackend.global.response.ApiResponse;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/commercial-estimated-sales")
public class CommercialEstimatedSalesCollectController
    implements CommercialEstimatedSalesCollectApi {

  private final CommercialDataCollectService commercialDataCollectService;

  @Override
  @PostMapping("/collect")
  public ResponseEntity<ApiResponse<CommercialEstimatedSalesCollectResponse>> collect(
      @Valid @RequestBody CommercialEstimatedSalesCollectRequest request) {
    CommercialDataCollectResult result = commercialDataCollectService.collect(request.toCommand());

    return ResponseEntity.ok(
        ApiResponse.success(CommercialEstimatedSalesCollectResponse.from(result)));
  }
}
