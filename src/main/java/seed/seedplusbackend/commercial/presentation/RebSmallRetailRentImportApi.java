package seed.seedplusbackend.commercial.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import seed.seedplusbackend.commercial.presentation.dto.RebSmallRetailRentImportResponse;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.swagger.annotation.ApiErrorCodeExamples;

@Tag(name = "한국부동산원 임대료 수집", description = "R-ONE 소규모상가 임대료 CSV 수동 적재 API")
@SecurityRequirement(name = "bearerAuth")
public interface RebSmallRetailRentImportApi {

  @Operation(summary = "소규모상가 임대료 CSV 수동 적재", operationId = "importRebSmallRetailRents")
  @ApiErrorCodeExamples({
    ErrorCode.INVALID_PARAMETER,
    ErrorCode.REB_RENT_FILE_INVALID,
    ErrorCode.REB_RENT_FILE_IMPORT_FAILED
  })
  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<ApiResponse<RebSmallRetailRentImportResponse>> importFile(
      @RequestPart("file") MultipartFile file,
      @RequestParam(name = "force", defaultValue = "false") boolean force);
}
