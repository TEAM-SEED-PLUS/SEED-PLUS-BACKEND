package seed.seedplusbackend.region.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.swagger.annotation.ApiErrorCodeExamples;
import seed.seedplusbackend.region.domain.entity.RegionCodeType;
import seed.seedplusbackend.region.presentation.dto.RegionResponse;

@Tag(name = "지역", description = "지역 관련 API")
public interface RegionApi {

  @Operation(summary = "지역 목록 조회 (계층 필터)", operationId = "getRegions")
  @ApiResponse(
      responseCode = "200",
      description = "성공",
      content =
          @Content(array = @ArraySchema(schema = @Schema(implementation = RegionResponse.class))))
  @GetMapping
  ResponseEntity<List<RegionResponse>> getRegions(
      @Parameter(description = "시도명 필터") @RequestParam(required = false) String sido,
      @Parameter(description = "시군구명 필터") @RequestParam(required = false) String sigungu,
      @Parameter(description = "코드 유형 필터") @RequestParam(required = false) RegionCodeType codeType);

  @Operation(summary = "지역 상세 조회", operationId = "getRegion")
  @ApiResponse(
      responseCode = "200",
      description = "성공",
      content = @Content(schema = @Schema(implementation = RegionResponse.class)))
  @ApiErrorCodeExamples({ErrorCode.NOT_FOUND_REGION})
  @GetMapping("/{regionId}")
  ResponseEntity<RegionResponse> getRegion(@Parameter(required = true) @PathVariable Long regionId);
}
