package seed.seedplusbackend.commercial.application.provider;

public enum CommercialDataType {
  SEOUL_REALTIME_CITY_POPULATION("SEOUL_REALTIME_CITY_POPULATION", "서울시 실시간 도시 인구"),
  SEOUL_SDOT_FOOT_TRAFFIC("SEOUL_SDOT_FOOT_TRAFFIC", "서울시 S-DoT 유동인구"),
  SEOUL_ESTIMATED_SALES("SEOUL_COMMERCIAL_ESTIMATED_SALES", "서울시 상권 추정매출"),
  SMALL_BUSINESS_STORE("SMALL_BUSINESS_STORE", "소상공인 상가정보"),
  REB_SMALL_RETAIL_RENT("REB_SMALL_RETAIL_RENT", "한국부동산원 소규모상가 임대료"),
  KOSIS_BUSINESS_SURVIVAL_RATE("KOSIS_BUSINESS_SURVIVAL_RATE", "KOSIS 산업별 신생기업 생존율"),
  SEOUL_REALTIME_CITY_POPULATION("SEOUL_REALTIME_CITY_POPULATION", "서울시 실시간 도시 인구"),
  SEOUL_SDOT_FOOT_TRAFFIC("SEOUL_SDOT_FOOT_TRAFFIC", "서울시 S-DoT 유동인구"),
  KOSIS_BUSINESS_COUNT("KOSIS_BUSINESS_COUNT", "KOSIS 산업별 기업수");

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
