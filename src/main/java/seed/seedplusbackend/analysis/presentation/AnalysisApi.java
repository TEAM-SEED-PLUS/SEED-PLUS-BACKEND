package seed.seedplusbackend.analysis.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import seed.seedplusbackend.analysis.presentation.dto.ProfitAnalysisRequest;
import seed.seedplusbackend.analysis.presentation.dto.ProfitAnalysisResponse;
import seed.seedplusbackend.analysis.presentation.dto.SurvivalAnalysisRequest;
import seed.seedplusbackend.analysis.presentation.dto.SurvivalAnalysisResponse;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.security.AuthenticatedUser;
import seed.seedplusbackend.global.swagger.annotation.ApiErrorCodeExamples;

@Tag(name = "수익률/생존률", description = "수익률/생존률 API")
public interface AnalysisApi {

  @Operation(
      summary = "수익률 분석",
      description = "DB에 저장된 최신 공공데이터로 계산합니다. 요청 시 공공 API를 재수집하지 않습니다.",
      operationId = "calculateProfitAnalysis",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiErrorCodeExamples({
    ErrorCode.INVALID_PARAMETER,
    ErrorCode.UNAUTHORIZED,
    ErrorCode.ANALYSIS_FUNCTION_CALL_FAILED
  })
  @PostMapping("/profit")
  ResponseEntity<ApiResponse<ProfitAnalysisResponse>> calculateProfit(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Valid @RequestBody ProfitAnalysisRequest request);

  @Operation(
      summary = "생존률 분석",
      description = "DB에 저장된 최신 공공데이터로 계산합니다. 요청 시 공공 API를 재수집하지 않습니다.",
      operationId = "calculateSurvivalAnalysis",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiErrorCodeExamples({
    ErrorCode.INVALID_PARAMETER,
    ErrorCode.UNAUTHORIZED,
    ErrorCode.ANALYSIS_FUNCTION_CALL_FAILED
  })
  @PostMapping("/survival")
  ResponseEntity<ApiResponse<SurvivalAnalysisResponse>> calculateSurvival(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Valid @RequestBody SurvivalAnalysisRequest request);
}
