package seed.seedplusbackend.analysis.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seed.seedplusbackend.analysis.application.AnalysisService;
import seed.seedplusbackend.analysis.presentation.dto.ProfitAnalysisRequest;
import seed.seedplusbackend.analysis.presentation.dto.ProfitAnalysisResponse;
import seed.seedplusbackend.analysis.presentation.dto.SurvivalAnalysisRequest;
import seed.seedplusbackend.analysis.presentation.dto.SurvivalAnalysisResponse;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.security.AuthenticatedUser;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/analysis")
public class AnalysisController implements AnalysisApi {

  private final AnalysisService analysisService;

  @Override
  public ResponseEntity<ApiResponse<ProfitAnalysisResponse>> calculateProfit(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Valid @RequestBody ProfitAnalysisRequest request) {
    Long userId = authenticatedUser == null ? null : authenticatedUser.getId();
    return ResponseEntity.ok(
        ApiResponse.success(
            ProfitAnalysisResponse.from(
                analysisService.calculateProfit(userId, request.toCommand()))));
  }

  @Override
  public ResponseEntity<ApiResponse<SurvivalAnalysisResponse>> calculateSurvival(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Valid @RequestBody SurvivalAnalysisRequest request) {
    Long userId = authenticatedUser == null ? null : authenticatedUser.getId();
    return ResponseEntity.ok(
        ApiResponse.success(
            SurvivalAnalysisResponse.from(
                analysisService.calculateSurvival(userId, request.toCommand()))));
  }
}
