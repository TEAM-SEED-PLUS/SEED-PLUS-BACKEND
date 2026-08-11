package seed.seedplusbackend.commercial.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import seed.seedplusbackend.commercial.presentation.dto.KosisBusinessSurvivalCollectRequest;
import seed.seedplusbackend.commercial.presentation.dto.KosisBusinessSurvivalCollectResponse;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.swagger.annotation.ApiErrorCodeExamples;

@Tag(name = "KOSIS 신생기업 생존율 수집", description = "KOSIS 산업별 신생기업 1~7년 생존율 데이터 수집 API")
@SecurityRequirement(name = "bearerAuth")
public interface KosisBusinessSurvivalCollectApi {

  @Operation(
      summary = "산업별 신생기업 생존율 수동 수집",
      description = "특정 연도 범위 또는 최신 N개 연도의 KOSIS 생존율 데이터를 수집하여 저장합니다.",
      operationId = "collectKosisBusinessSurvivalRates")
  @ApiErrorCodeExamples({
    ErrorCode.INVALID_PARAMETER,
    ErrorCode.KOSIS_OPEN_API_REQUEST_FAILED,
    ErrorCode.KOSIS_OPEN_API_INVALID_RESPONSE,
    ErrorCode.COMMERCIAL_DATA_PROVIDER_FAILED
  })
  @PostMapping("/collect")
  ResponseEntity<ApiResponse<KosisBusinessSurvivalCollectResponse>> collect(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "기간 조회 또는 최신 N개 연도 조회 중 하나를 선택합니다.",
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = KosisBusinessSurvivalCollectRequest.class),
                      examples = {
                        @ExampleObject(
                            name = "기간 조회",
                            value =
                                """
                                {
                                  "startYear": 2021,
                                  "endYear": 2022,
                                  "latestYearCount": null,
                                  "force": false
                                }
                                """),
                        @ExampleObject(
                            name = "최신 N개 연도 조회",
                            value =
                                """
                                {
                                  "startYear": null,
                                  "endYear": null,
                                  "latestYearCount": 3,
                                  "force": false
                                }
                                """)
                      }))
          @Valid
          @RequestBody
          KosisBusinessSurvivalCollectRequest request);
}
