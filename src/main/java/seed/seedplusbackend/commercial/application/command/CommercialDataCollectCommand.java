package seed.seedplusbackend.commercial.application.command;

import seed.seedplusbackend.commercial.application.provider.CommercialDataType;

public interface CommercialDataCollectCommand {

  CommercialDataType dataType();

  String targetKey();

  boolean force();
}
