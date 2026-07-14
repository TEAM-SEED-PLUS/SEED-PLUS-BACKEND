package seed.seedplusbackend.commercial.application.command;

import seed.seedplusbackend.commercial.application.provider.CommercialDataType;

public record CommercialEstimatedSalesCollectCommand(String stdrYyquCd, boolean force)
    implements CommercialDataCollectCommand {

  @Override
  public CommercialDataType dataType() {
    return CommercialDataType.SEOUL_ESTIMATED_SALES;
  }

  @Override
  public String targetKey() {
    return stdrYyquCd;
  }
}
