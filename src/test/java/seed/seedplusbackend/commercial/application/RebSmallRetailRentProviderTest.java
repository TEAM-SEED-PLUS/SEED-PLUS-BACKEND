package seed.seedplusbackend.commercial.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seed.seedplusbackend.commercial.application.command.RebSmallRetailRentImportCommand;
import seed.seedplusbackend.commercial.application.port.RebSmallRetailRentFileReaderPort;
import seed.seedplusbackend.commercial.application.port.RebSmallRetailRentStorePort;
import seed.seedplusbackend.commercial.application.provider.RebSmallRetailRentProvider;
import seed.seedplusbackend.commercial.application.result.RebSmallRetailRentFileResult;
import seed.seedplusbackend.commercial.application.result.RebSmallRetailRentPeriod;
import seed.seedplusbackend.commercial.application.result.RebSmallRetailRentRowResult;

@ExtendWith(MockitoExtension.class)
@DisplayName("한국부동산원 소규모상가 임대료 Provider")
class RebSmallRetailRentProviderTest {

  @Mock private RebSmallRetailRentFileReaderPort readerPort;
  @Mock private RebSmallRetailRentStorePort storePort;

  @Test
  @DisplayName("CSV를 읽어 분기 임대료를 교체하고 진행률을 기록한다")
  void collect_replacesImportedPeriods() {
    byte[] content = "csv".getBytes(StandardCharsets.UTF_8);
    RebSmallRetailRentImportCommand command =
        RebSmallRetailRentImportCommand.of("rent.csv", content, false);
    RebSmallRetailRentFileResult file = file();
    given(readerPort.read(content)).willReturn(file);
    List<Long> progress = new ArrayList<>();

    new RebSmallRetailRentProvider(readerPort, storePort)
        .collect(command, (total, fetched, cursor) -> progress.add(fetched));

    verify(storePort).replace("rent.csv", command.fileHash(), file);
    assertThat(progress).containsExactly(1L);
  }

  private RebSmallRetailRentFileResult file() {
    return new RebSmallRetailRentFileResult(
        List.of(new RebSmallRetailRentPeriod(2026, 1)),
        List.of(
            new RebSmallRetailRentRowResult(
                "key", 1, "전국", "전국", 1, 2026, 1, new BigDecimal("20.7"))));
  }
}
