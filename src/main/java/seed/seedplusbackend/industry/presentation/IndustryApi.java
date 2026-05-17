package seed.seedplusbackend.industry.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.swagger.annotation.ApiErrorCodeExamples;
import seed.seedplusbackend.industry.domain.entity.IndustryLevel;
import seed.seedplusbackend.industry.presentation.dto.IndustryResponse;

@Tag(name = "업종", description = "업종 API")
public interface IndustryApi {

  @Operation(summary = "업종 목록 조회 (계층 구조)", operationId = "getIndustries")
  @GetMapping
  ResponseEntity<ApiResponse<List<IndustryResponse>>> getIndustries(
      @Parameter(description = "업종 분류 레벨 필터") @RequestParam(required = false) IndustryLevel level,
      @Parameter(description = "상위 업종 ID (하위 업종 조회)") @RequestParam(required = false)
          Long parentId);

  @Operation(summary = "업종 상세 조회", operationId = "getIndustry")
  @ApiErrorCodeExamples({ErrorCode.NOT_FOUND_INDUSTRY})
  @GetMapping("/{industryId}")
  ResponseEntity<ApiResponse<IndustryResponse>> getIndustry(
      @Parameter(description = "업종 ID", example = "1") @PathVariable Long industryId);
}
