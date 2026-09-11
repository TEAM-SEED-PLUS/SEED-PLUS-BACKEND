package seed.seedplusbackend.commercial.infrastructure.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import seed.seedplusbackend.commercial.application.LatestEstimatedSalesQuarterResolver;
import seed.seedplusbackend.commercial.application.command.CommercialEstimatedSalesCollectCommand;
import seed.seedplusbackend.commercial.application.command.KosisBusinessCountCollectCommand;
import seed.seedplusbackend.commercial.application.command.SeoulRealtimeCityPopulationCollectCommand;
import seed.seedplusbackend.commercial.application.command.SmallBusinessStoreCollectCommand;
import seed.seedplusbackend.commercial.application.command.SmallBusinessStoreQueryType;
import seed.seedplusbackend.commercial.application.provider.CommercialDataType;

@DisplayName("공공데이터 배치 수집 명령 생성")
class PublicDataBatchCommandFactoryTest {

  private final LatestEstimatedSalesQuarterResolver quarterResolver =
      mock(LatestEstimatedSalesQuarterResolver.class);

  @Test
  @DisplayName("설정된 시군구별로 업종 필터 없이 수집 명령을 한 번씩 생성한다")
  void collectsEachConfiguredDistrictOnceWithoutIndustryFilters() {
    var factory = factory(List.of("11680", " 11680 ", "11740"), List.of());
    assertThat(factory.create(CommercialDataType.SMALL_BUSINESS_STORE))
        .containsExactly(
            new SmallBusinessStoreCollectCommand(
                "11680", null, null, null, true, SmallBusinessStoreQueryType.SIGUNGU),
            new SmallBusinessStoreCollectCommand(
                "11740", null, null, null, true, SmallBusinessStoreQueryType.SIGUNGU));
    verifyNoInteractions(quarterResolver);
  }

  @Test
  @DisplayName("예상 공표일 대신 실제 조회 가능한 최신 분기로 매출 수집 명령을 생성한다")
  void usesActualAvailableQuarterRatherThanPublicationDateAssumptions() {
    when(quarterResolver.resolve()).thenReturn("20262");
    assertThat(factory(List.of(), List.of()).create(CommercialDataType.SEOUL_ESTIMATED_SALES))
        .containsExactly(new CommercialEstimatedSalesCollectCommand("20262", true));
  }

  @Test
  @DisplayName("KOSIS 기업수는 최근 3개 연도의 정정 데이터를 재수집하도록 명령을 생성한다")
  void annualCollectionStillChecksLatestAvailableYearsAndCorrections() {
    assertThat(factory(List.of(), List.of()).create(CommercialDataType.KOSIS_BUSINESS_COUNT))
        .containsExactly(new KosisBusinessCountCollectCommand(null, null, 3, true));
  }

  @Test
  @DisplayName("도시데이터 수집 명령은 명시된 장소에 대해 중복 없이 생성한다")
  void cityCollectionUsesOnlyExplicitAreasWithoutDuplicates() {
    assertThat(
            factory(List.of(), List.of("POI001", " POI001 ", "POI002"))
                .create(CommercialDataType.SEOUL_REALTIME_CITY_POPULATION))
        .containsExactly(
            new SeoulRealtimeCityPopulationCollectCommand("POI001", true),
            new SeoulRealtimeCityPopulationCollectCommand("POI002", true));
  }

  @Test
  @DisplayName("수집 대상 미설정과 임대료 CSV 자동 수집 명령 생성을 거부한다")
  void missingTargetsAndManualCsvCannotBecomeSuccessfulEmptyJobs() {
    var factory = factory(List.of(), List.of());
    assertThatThrownBy(() -> factory.create(CommercialDataType.SMALL_BUSINESS_STORE))
        .isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(() -> factory.create(CommercialDataType.SEOUL_REALTIME_CITY_POPULATION))
        .isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(() -> factory.create(CommercialDataType.REB_SMALL_RETAIL_RENT))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("시군구 코드 대신 입력된 동 코드를 거부한다")
  void rejectsDongCodesWhereDistrictCodesAreRequired() {
    assertThatThrownBy(() -> factory(List.of("1168010100"), List.of()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private PublicDataBatchCommandFactory factory(List<String> districts, List<String> areas) {
    return new PublicDataBatchCommandFactory(
        new PublicDataCollectionProperties(ZoneId.of("Asia/Seoul"), Map.of(), districts, areas),
        quarterResolver);
  }
}
