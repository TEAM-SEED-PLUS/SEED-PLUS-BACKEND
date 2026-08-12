package seed.seedplusbackend.analysis.infrastructure;

import java.util.Locale;

final class MvpPublicDataMapping {
  private MvpPublicDataMapping() {}

  static Mapping resolve(String regionName, String industryName) {
    String region = normalize(regionName);
    String industry = normalize(industryName);
    String districtKeyword =
        region.contains("강남") || region.contains("gangnam") ? "강남" : regionName;
    String realtimePopulationArea =
        region.contains("강남") || region.contains("gangnam") ? "강남역" : regionName;
    String industryKeyword =
        industry.contains("카페") || industry.contains("cafe") || industry.contains("커피")
            ? "커피"
            : industryName;
    String kosisKeyword =
        industry.contains("카페") || industry.contains("cafe") || industry.contains("음식")
            ? "음식"
            : industryName;
    return new Mapping(districtKeyword, realtimePopulationArea, industryKeyword, kosisKeyword);
  }

  private static String normalize(String value) {
    return value == null ? "" : value.toLowerCase(Locale.ROOT).replace(" ", "");
  }

  record Mapping(
      String districtKeyword,
      String realtimePopulationArea,
      String industryKeyword,
      String kosisIndustryKeyword) {}
}
