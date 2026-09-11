package seed.seedplusbackend.commercial.infrastructure.batch;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.commercial.application.LatestEstimatedSalesQuarterResolver;
import seed.seedplusbackend.commercial.application.command.CommercialDataCollectCommand;
import seed.seedplusbackend.commercial.application.command.CommercialEstimatedSalesCollectCommand;
import seed.seedplusbackend.commercial.application.command.KosisBusinessCountCollectCommand;
import seed.seedplusbackend.commercial.application.command.KosisBusinessSurvivalCollectCommand;
import seed.seedplusbackend.commercial.application.command.SeoulRealtimeCityPopulationCollectCommand;
import seed.seedplusbackend.commercial.application.command.SeoulSdotFootTrafficCollectCommand;
import seed.seedplusbackend.commercial.application.command.SmallBusinessStoreCollectCommand;
import seed.seedplusbackend.commercial.application.command.SmallBusinessStoreQueryType;
import seed.seedplusbackend.commercial.application.provider.CommercialDataType;

@Component
@RequiredArgsConstructor
public class PublicDataBatchCommandFactory {

  private final PublicDataCollectionProperties properties;
  private final LatestEstimatedSalesQuarterResolver quarterResolver;

  // 완료 이력과 별개로 새 스케줄의 정정 데이터도 수집한다.
  // 동일 실행 슬롯 중복 방지는 Batch JobParameters/실행 잠금에서 담당해야 한다.
  public List<CommercialDataCollectCommand> create(CommercialDataType type) {
    return switch (type) {
      case SEOUL_ESTIMATED_SALES ->
          List.of(new CommercialEstimatedSalesCollectCommand(quarterResolver.resolve(), true));
      case SEOUL_SDOT_FOOT_TRAFFIC -> List.of(new SeoulSdotFootTrafficCollectCommand(true));
      case KOSIS_BUSINESS_COUNT ->
          List.of(new KosisBusinessCountCollectCommand(null, null, 3, true));
      case KOSIS_BUSINESS_SURVIVAL_RATE ->
          List.of(new KosisBusinessSurvivalCollectCommand(null, null, 3, true));
      case SMALL_BUSINESS_STORE -> storeCommands();
      case SEOUL_REALTIME_CITY_POPULATION -> cityCommands();
      case REB_SMALL_RETAIL_RENT -> throw new IllegalArgumentException("임대료 CSV는 기존 수동 적재를 사용합니다.");
    };
  }

  private List<CommercialDataCollectCommand> storeCommands() {
    requireTargets(properties.storeSigunguCodes(), "상가정보 시군구");
    // 한 시군구의 모든 업종을 한 번에 수집해 사용자별/업종별 중복 호출을 줄인다.
    return properties.storeSigunguCodes().stream()
        .<CommercialDataCollectCommand>map(
            code ->
                new SmallBusinessStoreCollectCommand(
                    code, null, null, null, true, SmallBusinessStoreQueryType.SIGUNGU))
        .toList();
  }

  private List<CommercialDataCollectCommand> cityCommands() {
    requireTargets(properties.cityAreas(), "실시간 도시데이터 장소");
    return properties.cityAreas().stream()
        .<CommercialDataCollectCommand>map(
            area -> new SeoulRealtimeCityPopulationCollectCommand(area, true))
        .toList();
  }

  private void requireTargets(List<String> targets, String name) {
    if (targets.isEmpty()) {
      throw new IllegalStateException(name + " 수집 대상이 설정되지 않았습니다.");
    }
  }
}
