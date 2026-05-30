package seed.seedplusbackend.industry.presentation;

import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.industry.application.IndustryQueryService;
import seed.seedplusbackend.industry.domain.entity.IndustryLevel;
import seed.seedplusbackend.industry.presentation.dto.IndustryResponse;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/industries")
public class IndustryController implements IndustryApi {

  private final IndustryQueryService industryQueryService;

  @Override
  public ResponseEntity<ApiResponse<List<IndustryResponse>>> getIndustries(
      @RequestParam(required = false) IndustryLevel level,
      @RequestParam(required = false) @Positive Long parentId) {
    return ResponseEntity.ok(
        ApiResponse.success(
            IndustryResponse.from(industryQueryService.getIndustries(level, parentId))));
  }

  @Override
  public ResponseEntity<ApiResponse<IndustryResponse>> getIndustry(
      @PathVariable @Positive Long industryId) {
    return ResponseEntity.ok(
        ApiResponse.success(IndustryResponse.from(industryQueryService.getIndustry(industryId))));
  }
}
