package seed.seedplusbackend.analysis.application.command;

import seed.seedplusbackend.commercial.application.command.SmallBusinessStoreQueryType;

public record SmallBusinessCollectionTarget(
    String commercialAreaCode,
    String largeIndustryCode,
    String mediumIndustryCode,
    String smallIndustryCode,
    SmallBusinessStoreQueryType queryType) {

  public SmallBusinessCollectionTarget(
      String commercialAreaCode,
      String largeIndustryCode,
      String mediumIndustryCode,
      String smallIndustryCode) {
    this(
        commercialAreaCode,
        largeIndustryCode,
        mediumIndustryCode,
        smallIndustryCode,
        SmallBusinessStoreQueryType.COMMERCIAL_AREA);
  }

  public SmallBusinessCollectionTarget {
    commercialAreaCode = requireText(commercialAreaCode, "소상공인 점포 조회 대상 코드");
    largeIndustryCode = normalize(largeIndustryCode);
    mediumIndustryCode = normalize(mediumIndustryCode);
    smallIndustryCode = normalize(smallIndustryCode);
    if (queryType == null) {
      throw new IllegalArgumentException("소상공인 점포 조회 유형은 필수입니다.");
    }
  }

  private static String requireText(String value, String fieldName) {
    String normalized = normalize(value);
    if (normalized == null) {
      throw new IllegalArgumentException(fieldName + "는 필수입니다.");
    }
    return normalized;
  }

  private static String normalize(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
