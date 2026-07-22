package seed.seedplusbackend.commercial.application.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.commercial.application.command.CommercialDataCollectCommand;
import seed.seedplusbackend.commercial.application.command.RebSmallRetailRentImportCommand;
import seed.seedplusbackend.commercial.application.port.RebSmallRetailRentFileReaderPort;
import seed.seedplusbackend.commercial.application.port.RebSmallRetailRentStorePort;
import seed.seedplusbackend.commercial.application.result.RebSmallRetailRentFileResult;

@Component
@RequiredArgsConstructor
public class RebSmallRetailRentProvider implements CommercialDataProvider {

  private final RebSmallRetailRentFileReaderPort fileReaderPort;
  private final RebSmallRetailRentStorePort storePort;

  @Override
  public CommercialDataType supports() {
    return CommercialDataType.REB_SMALL_RETAIL_RENT;
  }

  @Override
  public void collect(CommercialDataCollectCommand command, CollectProgress progress) {
    RebSmallRetailRentImportCommand importCommand = cast(command);
    RebSmallRetailRentFileResult file = fileReaderPort.read(importCommand.fileContent());

    storePort.replace(importCommand.originalFileName(), importCommand.fileHash(), file);
    progress.update(file.rows().size(), file.rows().size(), file.periods().size());
  }

  private RebSmallRetailRentImportCommand cast(CommercialDataCollectCommand command) {
    if (command instanceof RebSmallRetailRentImportCommand importCommand) {
      return importCommand;
    }
    throw new IllegalArgumentException("잘못된 요청이 한국부동산원 임대료 Provider에 전달되었습니다.");
  }
}
