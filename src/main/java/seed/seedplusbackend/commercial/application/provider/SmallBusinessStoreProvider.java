package seed.seedplusbackend.commercial.application.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.commercial.application.command.CommercialDataCollectCommand;
import seed.seedplusbackend.commercial.application.command.SmallBusinessStoreCollectCommand;
import seed.seedplusbackend.commercial.application.port.SmallBusinessStoreClientPort;
import seed.seedplusbackend.commercial.application.port.SmallBusinessStoreStorePort;
import seed.seedplusbackend.commercial.application.result.SmallBusinessStorePageResult;
import seed.seedplusbackend.commercial.infrastructure.client.SmallBusinessStoreOpenApiProperties;

@Component
@RequiredArgsConstructor
public class SmallBusinessStoreProvider implements CommercialDataProvider {

  private final SmallBusinessStoreClientPort clientPort;
  private final SmallBusinessStoreStorePort storePort;
  private final SmallBusinessStoreOpenApiProperties properties;

  @Override
  public CommercialDataType supports() {
    return CommercialDataType.SMALL_BUSINESS_STORE;
  }

  @Override
  public void collect(CommercialDataCollectCommand command, CollectProgress progress) {
    SmallBusinessStoreCollectCommand storeCommand = cast(command);
    int pageNumber = 1;
    long fetchedCount = 0;

    while (true) {
      SmallBusinessStorePageResult page =
          clientPort.fetch(storeCommand, pageNumber, properties.pageSize());

      if (page.rows().isEmpty()) {
        progress.update(page.totalCount(), fetchedCount, pageNumber);
        return;
      }

      storePort.upsertAll(storeCommand.commercialAreaCode(), page.rows());
      fetchedCount += page.rows().size();
      progress.update(page.totalCount(), fetchedCount, pageNumber);

      if (fetchedCount >= page.totalCount()) {
        return;
      }
      pageNumber++;
    }
  }

  private SmallBusinessStoreCollectCommand cast(CommercialDataCollectCommand command) {
    if (command instanceof SmallBusinessStoreCollectCommand storeCommand) {
      return storeCommand;
    }
    throw new IllegalArgumentException("소상공인 상가정보 Provider에 잘못된 요청이 전달되었습니다.");
  }
}
