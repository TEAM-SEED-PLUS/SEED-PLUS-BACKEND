package seed.seedplusbackend.commercial.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import seed.seedplusbackend.commercial.application.CommercialDataCollectService;
import seed.seedplusbackend.commercial.application.command.CommercialDataCollectCommand;
import seed.seedplusbackend.commercial.application.command.RebSmallRetailRentImportCommand;
import seed.seedplusbackend.commercial.application.result.CommercialDataCollectResult;
import seed.seedplusbackend.commercial.domain.entity.CommercialDataCollectStatus;
import seed.seedplusbackend.global.error.GlobalExceptionHandler;

@ExtendWith(MockitoExtension.class)
@DisplayName("한국부동산원 소규모상가 임대료 수동 적재 API")
class RebSmallRetailRentImportControllerTest {

  private MockMvc mockMvc;

  @Mock private CommercialDataCollectService collectService;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new RebSmallRetailRentImportController(collectService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  @DisplayName("CSV 파일을 multipart 요청으로 적재한다")
  void importFile_importsCsv() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "rent.csv", "text/csv", "csv".getBytes());
    given(collectService.collect(any(CommercialDataCollectCommand.class)))
        .willReturn(
            new CommercialDataCollectResult(
                "REB_SMALL_RETAIL_RENT",
                "hash",
                276,
                276,
                false,
                CommercialDataCollectStatus.COMPLETED,
                "completed"));

    mockMvc
        .perform(
            multipart("/api/v1/reb-small-retail-rents/import").file(file).param("force", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.importedCount").value(276))
        .andExpect(jsonPath("$.data.skipped").value(false));

    verify(collectService)
        .collect(
            argThat(
                command ->
                    command instanceof RebSmallRetailRentImportCommand importCommand
                        && importCommand.originalFileName().equals("rent.csv")
                        && importCommand.force()));
  }

  @Test
  @DisplayName("CSV가 아닌 파일은 거절한다")
  void importFile_rejectsNonCsvFile() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "rent.xlsx", "application/octet-stream", new byte[] {1});

    mockMvc
        .perform(multipart("/api/v1/reb-small-retail-rents/import").file(file))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(9400));

    verify(collectService, never()).collect(any());
  }

  @Test
  @DisplayName("file multipart 파트가 없으면 잘못된 파일 요청으로 응답한다")
  void importFile_rejectsMissingFilePart() throws Exception {
    mockMvc
        .perform(multipart("/api/v1/reb-small-retail-rents/import"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(9400));

    verify(collectService, never()).collect(any());
  }
}
