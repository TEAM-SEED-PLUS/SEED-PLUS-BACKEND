package seed.seedplusbackend.commercial.application.port;

import seed.seedplusbackend.commercial.application.command.SmallBusinessStoreCollectCommand;
import seed.seedplusbackend.commercial.application.result.SmallBusinessStorePageResult;

public interface SmallBusinessStoreClientPort {

  SmallBusinessStorePageResult fetch(
      SmallBusinessStoreCollectCommand command, int pageNumber, int numberOfRows);
}
