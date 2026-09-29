package seed.seedplusbackend.commercial.infrastructure.batch;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.scheduling.support.CronExpression;
import seed.seedplusbackend.commercial.application.provider.CommercialDataType;

@ConfigurationProperties("public-data.collection")
public record PublicDataCollectionProperties(
    @DefaultValue("Asia/Seoul") ZoneId zone,
    Map<CommercialDataType, String> crons,
    List<String> storeSigunguCodes,
    List<String> cityAreas) {

  public PublicDataCollectionProperties {
    if (zone == null) {
      throw new IllegalArgumentException("공공데이터 수집 시간대는 필수입니다.");
    }
    crons = crons == null ? Map.of() : Map.copyOf(crons);
    crons.forEach(
        (type, cron) -> {
          if (type == CommercialDataType.REB_SMALL_RETAIL_RENT) {
            throw new IllegalArgumentException("임대료 CSV는 자동 수집 대상이 아닙니다.");
          }
          if (!"-".equals(cron)) {
            CronExpression.parse(cron);
          }
        });
    storeSigunguCodes = normalized(storeSigunguCodes);
    cityAreas = normalized(cityAreas);
    if (storeSigunguCodes.stream().anyMatch(code -> !code.matches("[0-9]{5}"))) {
      throw new IllegalArgumentException("상가정보 수집 지역은 5자리 시군구 코드여야 합니다.");
    }
    if (cityAreas.stream().anyMatch(area -> area.length() > 80)) {
      throw new IllegalArgumentException("도시데이터 장소는 80자 이하여야 합니다.");
    }
  }

  private static List<String> normalized(List<String> values) {
    return values == null
        ? List.of()
        : values.stream().map(String::trim).filter(value -> !value.isEmpty()).distinct().toList();
  }
}
