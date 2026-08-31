package seed.seedplusbackend.commercial.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seed.seedplusbackend.commercial.application.CommercialDataCollectService;
import seed.seedplusbackend.commercial.presentation.dto.SmallBusinessStoreCollectRequest;
import seed.seedplusbackend.commercial.presentation.dto.SmallBusinessStoreCollectResponse;
import seed.seedplusbackend.global.response.ApiResponse;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/small-business-stores")
public class SmallBusinessStoreCollectController implements SmallBusinessStoreCollectApi {

  private final CommercialDataCollectService collectService;

  @Override
  public ResponseEntity<ApiResponse<SmallBusinessStoreCollectResponse>> collect(
      @Valid @RequestBody SmallBusinessStoreCollectRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            SmallBusinessStoreCollectResponse.from(collectService.collect(request.toCommand()))));
  }
}
