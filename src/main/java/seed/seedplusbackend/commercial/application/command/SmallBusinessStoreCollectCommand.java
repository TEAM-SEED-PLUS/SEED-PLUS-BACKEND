package seed.seedplusbackend.commercial.application.command;

import seed.seedplusbackend.commercial.application.provider.CommercialDataType;

public record SmallBusinessStoreCollectCommand(
    String commercialAreaCode,
    String largeIndustryCode,
    String mediumIndustryCode,
    String smallIndustryCode,
    boolean force,
    SmallBusinessStoreQueryType queryType)
    implements CommercialDataCollectCommand {

  public SmallBusinessStoreCollectCommand(
      String commercialAreaCode,
      String largeIndustryCode,
      String mediumIndustryCode,
      String smallIndustryCode,
      boolean force) {
    this(
        commercialAreaCode,
        largeIndustryCode,
        mediumIndustryCode,
        smallIndustryCode,
        force,
        SmallBusinessStoreQueryType.COMMERCIAL_AREA);
  }

  public SmallBusinessStoreCollectCommand {
    if (queryType == null) {
      throw new IllegalArgumentException("소상공인 점포 조회 유형은 필수입니다.");
    }
  }

  @Override
  public CommercialDataType dataType() {
    return CommercialDataType.SMALL_BUSINESS_STORE;
  }

  public String targetKey() {
    return String.join(
        ":",
        commercialAreaCode,
        queryType.name(),
        valueOrAll(largeIndustryCode),
        valueOrAll(mediumIndustryCode),
        valueOrAll(smallIndustryCode));
  }

  private String valueOrAll(String value) {
    return value == null || value.isBlank() ? "ALL" : value;
  }
}
