package seed.seedplusbackend.commercial.application.command;

import seed.seedplusbackend.commercial.application.provider.CommercialDataType;

public record SmallBusinessStoreCollectCommand(
    String commercialAreaCode,
    String largeIndustryCode,
    String mediumIndustryCode,
    String smallIndustryCode,
    boolean force)
    implements CommercialDataCollectCommand {

  @Override
  public CommercialDataType dataType() {
    return CommercialDataType.SMALL_BUSINESS_STORE;
  }

  public String targetKey() {
    return String.join(
        ":",
        commercialAreaCode,
        valueOrAll(largeIndustryCode),
        valueOrAll(mediumIndustryCode),
        valueOrAll(smallIndustryCode));
  }

  private String valueOrAll(String value) {
    return value == null || value.isBlank() ? "ALL" : value;
  }
}
