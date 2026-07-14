package seed.seedplusbackend.commercial.application.provider;

public enum CommercialDataType {
  SEOUL_ESTIMATED_SALES("SEOUL_COMMERCIAL_ESTIMATED_SALES", "서울시 상권 추정매출"),
  SMALL_BUSINESS_STORE("SMALL_BUSINESS_STORE", "소상공인 상가정보");

  private final String historyKey;
  private final String displayName;

  CommercialDataType(String historyKey, String displayName) {
    this.historyKey = historyKey;
    this.displayName = displayName;
  }

  public String historyKey() {
    return historyKey;
  }

  public String displayName() {
    return displayName;
  }
}
