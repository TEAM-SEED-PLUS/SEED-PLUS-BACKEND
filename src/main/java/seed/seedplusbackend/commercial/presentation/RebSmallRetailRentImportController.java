package seed.seedplusbackend.commercial.presentation;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import seed.seedplusbackend.commercial.application.CommercialDataCollectService;
import seed.seedplusbackend.commercial.application.command.RebSmallRetailRentImportCommand;
import seed.seedplusbackend.commercial.presentation.dto.RebSmallRetailRentImportResponse;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.response.ApiResponse;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reb-small-retail-rents")
public class RebSmallRetailRentImportController implements RebSmallRetailRentImportApi {

  private final CommercialDataCollectService collectService;

  @Override
  public ResponseEntity<ApiResponse<RebSmallRetailRentImportResponse>> importFile(
      MultipartFile file, boolean force) {
    try {
      RebSmallRetailRentImportCommand command =
          RebSmallRetailRentImportCommand.of(file.getOriginalFilename(), file.getBytes(), force);
      return ResponseEntity.ok(
          ApiResponse.success(
              RebSmallRetailRentImportResponse.from(collectService.collect(command))));
    } catch (IOException exception) {
      throw new ApplicationException(
          ErrorCode.REB_RENT_FILE_INVALID, "업로드 파일을 읽을 수 없습니다.", exception);
    }
  }
}
