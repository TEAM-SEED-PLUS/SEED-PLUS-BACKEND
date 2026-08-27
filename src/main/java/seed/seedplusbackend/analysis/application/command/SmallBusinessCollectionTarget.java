package seed.seedplusbackend.analysis.application.command;

public record SmallBusinessCollectionTarget(
    String commercialAreaCode,
    String largeIndustryCode,
    String mediumIndustryCode,
    String smallIndustryCode) {

  public SmallBusinessCollectionTarget {
    commercialAreaCode = requireText(commercialAreaCode, "상권번호");
    largeIndustryCode = normalize(largeIndustryCode);
    mediumIndustryCode = normalize(mediumIndustryCode);
    smallIndustryCode = normalize(smallIndustryCode);
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
