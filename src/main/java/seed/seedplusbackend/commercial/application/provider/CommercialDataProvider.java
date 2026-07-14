package seed.seedplusbackend.commercial.application.provider;

import seed.seedplusbackend.commercial.application.command.CommercialDataCollectCommand;

public interface CommercialDataProvider {

  CommercialDataType supports();

  void collect(CommercialDataCollectCommand command, CollectProgress progress);
}
