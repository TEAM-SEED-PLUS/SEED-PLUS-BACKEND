package seed.seedplusbackend.analysis.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seed.seedplusbackend.analysis.application.command.ProfitAnalysisCommand;
import seed.seedplusbackend.analysis.application.command.ProfitAnalysisLambdaCommand;
import seed.seedplusbackend.analysis.application.command.SurvivalAnalysisCommand;
import seed.seedplusbackend.analysis.application.command.SurvivalAnalysisLambdaCommand;
import seed.seedplusbackend.analysis.application.port.AnalysisLambdaClient;
import seed.seedplusbackend.analysis.application.port.PublicDataResolver;
import seed.seedplusbackend.analysis.application.result.AnalysisDataCollectionResult;
import seed.seedplusbackend.analysis.application.result.ProfitAnalysisResult;
import seed.seedplusbackend.analysis.application.result.PublicDataMetrics;
import seed.seedplusbackend.analysis.application.result.SurvivalAnalysisResult;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionRunStatus;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionType;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.industry.domain.entity.IndustryLevel;
import seed.seedplusbackend.industry.domain.entity.IndustryStatus;
import seed.seedplusbackend.industry.domain.repository.IndustryRepository;
import seed.seedplusbackend.region.application.RegionResolver;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.domain.entity.RegionCodeType;
import seed.seedplusbackend.region.domain.repository.RegionRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalysisService")
class AnalysisServiceTest {

  private AnalysisService analysisService;

  @Mock private AnalysisLambdaClient analysisLambdaClient;
  @Mock private RegionRepository regionRepository;
  @Mock private IndustryRepository industryRepository;
  @Mock private PublicDataResolver publicDataResolver;
  @Mock private AnalysisDataCollectionCoordinator collectionCoordinator;

  @BeforeEach
  void setUp() {
    analysisService =
        new AnalysisService(
            analysisLambdaClient,
            new RegionResolver(regionRepository),
            industryRepository,
            publicDataResolver,
            collectionCoordinator);
    org.mockito.Mockito.lenient()
        .when(
            collectionCoordinator.collect(
                anyLong(), any(AnalysisCollectionType.class), anyString(), anyString()))
        .thenReturn(
            new AnalysisDataCollectionResult(
                1L, AnalysisCollectionRunStatus.COMPLETED, java.util.List.of()));
    org.mockito.Mockito.lenient()
        .when(
            publicDataResolver.resolve(
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
        .thenReturn(metrics());
  }

  @Test
  @DisplayName("Profit analysis recalculates after collecting the latest public data")
  void calculateProfit_recalculatesAfterCollectingLatestData() {
    ProfitAnalysisCommand firstCommand =
        new ProfitAnalysisCommand(
            "스타카페",
            " I561 ",
            "1168010100",
            new BigDecimal("30.0"),
            new BigDecimal("5000"),
            new BigDecimal("300"),
            new BigDecimal("2000"),
            3);
    ProfitAnalysisCommand secondCommand =
        new ProfitAnalysisCommand(
            "스타카페",
            "I561",
            "1168010100",
            new BigDecimal("30"),
            new BigDecimal("5000.0"),
            new BigDecimal("300.00"),
            new BigDecimal("2000"),
            3);
    ProfitAnalysisResult result = profitResult();
    given(regionRepository.findByCodeAndCodeType("1168010100", RegionCodeType.LEGAL_DONG))
        .willReturn(java.util.Optional.of(region()));
    given(industryRepository.findByIndustryCodeAndStatus("I561", IndustryStatus.ACTIVE))
        .willReturn(java.util.Optional.of(industry("I561", "Cafe")));
    given(analysisLambdaClient.requestProfit(anyProfitLambdaCommand())).willReturn(result);

    ProfitAnalysisResult first = analysisService.calculateProfit(1L, firstCommand);
    ProfitAnalysisResult second = analysisService.calculateProfit(2L, secondCommand);

    assertThat(first).isSameAs(result);
    assertThat(second).isSameAs(result);
    verify(collectionCoordinator).collect(1L, AnalysisCollectionType.PROFIT, "1168010100", "I561");
    verify(collectionCoordinator).collect(2L, AnalysisCollectionType.PROFIT, "1168010100", "I561");
    verify(analysisLambdaClient, times(2))
        .requestProfit(
            org.mockito.ArgumentMatchers.argThat(
                command ->
                    command != null
                        && "Cafe".equals(command.industry())
                        && "Seoul Gangnam-gu Yeoksam-dong".equals(command.region())));
  }

  @Test
  @DisplayName("지역 기준 데이터가 없으면 계산하지 않는다")
  void calculateProfit_rejectsMissingRegion() {
    ProfitAnalysisCommand command =
        new ProfitAnalysisCommand(
            "강남스타카페",
            "I21201",
            "1168010100",
            new BigDecimal("40"),
            new BigDecimal("8000"),
            new BigDecimal("250"),
            new BigDecimal("2000"),
            3);
    given(regionRepository.findByCodeAndCodeType("1168010100", RegionCodeType.LEGAL_DONG))
        .willReturn(java.util.Optional.empty());

    assertThatThrownBy(() -> analysisService.calculateProfit(1L, command))
        .isInstanceOf(ApplicationException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.NOT_FOUND_REGION);
    verify(publicDataResolver, never()).resolve(anyString(), anyString());
    verify(analysisLambdaClient, never()).requestProfit(anyProfitLambdaCommand());
  }

  @Test
  @DisplayName("활성 업종 기준 데이터가 없으면 계산하지 않는다")
  void calculateProfit_rejectsMissingIndustry() {
    ProfitAnalysisCommand command =
        new ProfitAnalysisCommand(
            "강남스타카페",
            "I21201",
            "1168010100",
            new BigDecimal("40"),
            new BigDecimal("8000"),
            new BigDecimal("250"),
            new BigDecimal("2000"),
            3);
    given(regionRepository.findByCodeAndCodeType("1168010100", RegionCodeType.LEGAL_DONG))
        .willReturn(java.util.Optional.of(region()));
    given(industryRepository.findByIndustryCodeAndStatus("I21201", IndustryStatus.ACTIVE))
        .willReturn(java.util.Optional.empty());

    assertThatThrownBy(() -> analysisService.calculateProfit(1L, command))
        .isInstanceOf(ApplicationException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.NOT_FOUND_INDUSTRY);
    verify(publicDataResolver, never()).resolve(anyString(), anyString());
    verify(analysisLambdaClient, never()).requestProfit(anyProfitLambdaCommand());
  }

  @Test
  @DisplayName("Survival analysis recalculates after collecting the latest public data")
  void calculateSurvival_recalculatesAfterCollectingLatestData() {
    SurvivalAnalysisCommand command = survivalCommand();
    SurvivalAnalysisResult result = survivalResult();
    given(regionRepository.findByCodeAndCodeType("1168010100", RegionCodeType.LEGAL_DONG))
        .willReturn(java.util.Optional.of(region()));
    given(industryRepository.findByIndustryCodeAndStatus("I562", IndustryStatus.ACTIVE))
        .willReturn(java.util.Optional.of(industry("I562", "Restaurant")));
    given(analysisLambdaClient.requestSurvival(anySurvivalLambdaCommand())).willReturn(result);

    SurvivalAnalysisResult first = analysisService.calculateSurvival(1L, command);
    SurvivalAnalysisResult second = analysisService.calculateSurvival(1L, command);

    assertThat(first).isSameAs(result);
    assertThat(second).isSameAs(result);
    verify(collectionCoordinator, times(2))
        .collect(1L, AnalysisCollectionType.SURVIVAL, "1168010100", "I562");
    verify(analysisLambdaClient, times(2))
        .requestSurvival(
            org.mockito.ArgumentMatchers.argThat(
                lambdaCommand ->
                    lambdaCommand != null
                        && "Restaurant".equals(lambdaCommand.industry())
                        && "Seoul Gangnam-gu Yeoksam-dong".equals(lambdaCommand.region())));
  }

  @Test
  @DisplayName("수익률 공공데이터가 없으면 명세의 fallback 값을 전달한다")
  void calculateProfit_appliesFallbackValues() {
    ProfitAnalysisCommand command =
        new ProfitAnalysisCommand(
            "스타카페",
            "I561",
            "1168010100",
            new BigDecimal("30"),
            new BigDecimal("5000"),
            new BigDecimal("300"),
            new BigDecimal("2000"),
            3);
    given(regionRepository.findByCodeAndCodeType("1168010100", RegionCodeType.LEGAL_DONG))
        .willReturn(java.util.Optional.of(region()));
    given(industryRepository.findByIndustryCodeAndStatus("I561", IndustryStatus.ACTIVE))
        .willReturn(java.util.Optional.of(industry("I561", "Cafe")));
    given(publicDataResolver.resolve("1168010100", "I561"))
        .willReturn(metricsWithoutFallbackTargets());
    given(analysisLambdaClient.requestProfit(anyProfitLambdaCommand())).willReturn(profitResult());

    analysisService.calculateProfit(1L, command);

    verify(analysisLambdaClient)
        .requestProfit(
            org.mockito.ArgumentMatchers.argThat(
                lambdaCommand ->
                    lambdaCommand.storeZoneOne() == 100
                        && lambdaCommand.storeListInArea() == 300
                        && lambdaCommand.storeListInRadius() == 50
                        && lambdaCommand.fallbackUsed()));
  }

  @Test
  @DisplayName("생존률 공공데이터가 없으면 명세의 fallback 값을 전달한다")
  void calculateSurvival_appliesFallbackValues() {
    SurvivalAnalysisCommand command = survivalCommand();
    given(regionRepository.findByCodeAndCodeType("1168010100", RegionCodeType.LEGAL_DONG))
        .willReturn(java.util.Optional.of(region()));
    given(industryRepository.findByIndustryCodeAndStatus("I562", IndustryStatus.ACTIVE))
        .willReturn(java.util.Optional.of(industry("I562", "Restaurant")));
    given(publicDataResolver.resolve("1168010100", "I562"))
        .willReturn(metricsWithoutFallbackTargets());
    given(analysisLambdaClient.requestSurvival(anySurvivalLambdaCommand()))
        .willReturn(survivalResult());

    analysisService.calculateSurvival(1L, command);

    verify(analysisLambdaClient)
        .requestSurvival(
            org.mockito.ArgumentMatchers.argThat(
                lambdaCommand ->
                    BigDecimal.ZERO.compareTo(lambdaCommand.salesGrowthRate()) == 0
                        && lambdaCommand.storeDensity() == 40
                        && new BigDecimal("8.0").compareTo(lambdaCommand.vacancyRate()) == 0
                        && lambdaCommand.trafficIndex() == 14000
                        && lambdaCommand.fallbackUsed()));
  }

  @Test
  @DisplayName("Lambda 호출 실패 후 다음 요청에서 다시 분석한다")
  void calculateProfit_retriesAnalysisOnNextRequestAfterLambdaFailure() {
    ProfitAnalysisCommand command =
        new ProfitAnalysisCommand(
            "스타카페",
            "I561",
            "1168010100",
            new BigDecimal("30"),
            new BigDecimal("5000"),
            new BigDecimal("300"),
            new BigDecimal("2000"),
            3);
    ProfitAnalysisResult result = profitResult();
    given(regionRepository.findByCodeAndCodeType("1168010100", RegionCodeType.LEGAL_DONG))
        .willReturn(java.util.Optional.of(region()));
    given(industryRepository.findByIndustryCodeAndStatus("I561", IndustryStatus.ACTIVE))
        .willReturn(java.util.Optional.of(industry("I561", "Cafe")));
    given(analysisLambdaClient.requestProfit(anyProfitLambdaCommand()))
        .willThrow(new ApplicationException(ErrorCode.ANALYSIS_FUNCTION_CALL_FAILED))
        .willReturn(result);

    assertThatThrownBy(() -> analysisService.calculateProfit(1L, command))
        .isInstanceOf(ApplicationException.class);
    ProfitAnalysisResult recovered = analysisService.calculateProfit(1L, command);

    assertThat(recovered).isSameAs(result);
    verify(analysisLambdaClient, times(2)).requestProfit(anyProfitLambdaCommand());
  }

  @Test
  @DisplayName("공공데이터 수집이 실패하면 분석 함수를 호출하지 않는다")
  void calculateProfit_doesNotAnalyzeWhenCollectionFails() {
    ProfitAnalysisCommand command =
        new ProfitAnalysisCommand(
            "스타카페",
            "I561",
            "1168010100",
            new BigDecimal("30"),
            new BigDecimal("5000"),
            new BigDecimal("300"),
            new BigDecimal("2000"),
            3);
    given(collectionCoordinator.collect(1L, AnalysisCollectionType.PROFIT, "1168010100", "I561"))
        .willReturn(
            new AnalysisDataCollectionResult(
                7L,
                AnalysisCollectionRunStatus.FAILED,
                java.util.List.of("SEOUL_ESTIMATED_SALES")));

    assertThatThrownBy(() -> analysisService.calculateProfit(1L, command))
        .isInstanceOf(ApplicationException.class)
        .hasMessageContaining("runId=7")
        .hasMessageContaining("SEOUL_ESTIMATED_SALES")
        .extracting("errorCode")
        .isEqualTo(ErrorCode.ANALYSIS_DATA_COLLECTION_FAILED);
    verify(publicDataResolver, never()).resolve(anyString(), anyString());
    verify(analysisLambdaClient, never()).requestProfit(anyProfitLambdaCommand());
  }

  @Test
  @DisplayName("수집 실행 ID가 있으면 실패 작업을 재시도한 뒤 수익률을 계산한다")
  void calculateProfit_retriesCollectionRunBeforeAnalysis() {
    ProfitAnalysisCommand command =
        new ProfitAnalysisCommand(
            "스타카페",
            "I561",
            "1168010100",
            new BigDecimal("30"),
            new BigDecimal("5000"),
            new BigDecimal("300"),
            new BigDecimal("2000"),
            3,
            7L);
    ProfitAnalysisResult result = profitResult();
    given(collectionCoordinator.retry(1L, 7L, AnalysisCollectionType.PROFIT, "1168010100", "I561"))
        .willReturn(
            new AnalysisDataCollectionResult(
                7L, AnalysisCollectionRunStatus.COMPLETED, java.util.List.of()));
    given(regionRepository.findByCodeAndCodeType("1168010100", RegionCodeType.LEGAL_DONG))
        .willReturn(java.util.Optional.of(region()));
    given(industryRepository.findByIndustryCodeAndStatus("I561", IndustryStatus.ACTIVE))
        .willReturn(java.util.Optional.of(industry("I561", "Cafe")));
    given(analysisLambdaClient.requestProfit(anyProfitLambdaCommand())).willReturn(result);

    assertThat(analysisService.calculateProfit(1L, command)).isSameAs(result);
    verify(collectionCoordinator, never())
        .collect(1L, AnalysisCollectionType.PROFIT, "1168010100", "I561");
    verify(analysisLambdaClient).requestProfit(anyProfitLambdaCommand());
  }

  private SurvivalAnalysisCommand survivalCommand() {
    return new SurvivalAnalysisCommand(
        "강남스타카페",
        "I562",
        "1168010100",
        new BigDecimal("40"),
        new BigDecimal("8000"),
        new BigDecimal("250"),
        new BigDecimal("2000"),
        3);
  }

  private PublicDataMetrics metrics() {
    return new PublicDataMetrics(
        3120000000L,
        104,
        new BigDecimal("3500000"),
        new BigDecimal("2900000"),
        100,
        300,
        50,
        18,
        new BigDecimal("5.2"),
        42,
        new BigDecimal("8"),
        14000,
        new BigDecimal("68"),
        new BigDecimal("120"),
        new BigDecimal("1500"),
        new BigDecimal("180"),
        true,
        java.util.List.of("서울시 상권분석"));
  }

  private PublicDataMetrics metricsWithoutFallbackTargets() {
    return new PublicDataMetrics(
        3120000000L,
        104,
        new BigDecimal("3500000"),
        new BigDecimal("2900000"),
        null,
        null,
        null,
        18,
        null,
        null,
        null,
        null,
        new BigDecimal("68"),
        new BigDecimal("120"),
        new BigDecimal("1500"),
        new BigDecimal("180"),
        false,
        java.util.List.of("서울시 상권분석"));
  }

  private ProfitAnalysisResult profitResult() {
    return new ProfitAnalysisResult(
        new ProfitAnalysisResult.ProfitInput(
            "food",
            "gangnam",
            new BigDecimal("30.0"),
            new BigDecimal("5000.0"),
            new BigDecimal("300.0"),
            new BigDecimal("2000.0"),
            3),
        new ProfitAnalysisResult.ProfitAssumptions(
            new BigDecimal("80"),
            new BigDecimal("1.3"),
            new BigDecimal("18"),
            new BigDecimal("42"),
            new BigDecimal("8"),
            new BigDecimal("250")),
        new ProfitAnalysisResult.ProfitResult(
            new BigDecimal("3120"),
            new BigDecimal("750"),
            new BigDecimal("250"),
            new BigDecimal("1300.0"),
            new BigDecimal("1310"),
            new BigDecimal("24"),
            new BigDecimal("10"),
            new BigDecimal("42"),
            new BigDecimal("8"),
            new BigDecimal("16"),
            new BigDecimal("499"),
            new BigDecimal("7000.0"),
            new BigDecimal("14"),
            new BigDecimal("81")));
  }

  private SurvivalAnalysisResult survivalResult() {
    return new SurvivalAnalysisResult(
        new SurvivalAnalysisResult.SurvivalInput(
            "gangnam",
            "cafe",
            new BigDecimal("40.0"),
            new BigDecimal("250.0"),
            new BigDecimal("2000.0"),
            new BigDecimal("4.0"),
            new BigDecimal("3.0"),
            new BigDecimal("4.0"),
            new BigDecimal("2.0"),
            new BigDecimal("4.0"),
            new BigDecimal("2.0"),
            "transfer",
            new BigDecimal("4200.0")),
        new SurvivalAnalysisResult.SurvivalDerived(
            new BigDecimal("3360"),
            new BigDecimal("80"),
            new BigDecimal("74"),
            new BigDecimal("62"),
            new BigDecimal("75")),
        new SurvivalAnalysisResult.SurvivalScoreBreakdown(
            new BigDecimal("15"),
            new BigDecimal("2"),
            new BigDecimal("-12"),
            new BigDecimal("0"),
            new BigDecimal("8"),
            new BigDecimal("-20"),
            new BigDecimal("1"),
            new BigDecimal("5"),
            new BigDecimal("49"),
            new BigDecimal("49")),
        new SurvivalAnalysisResult.SurvivalResult("caution", "40 ~ 55%", "20 ~ 35%"));
  }

  private ProfitAnalysisLambdaCommand anyProfitLambdaCommand() {
    return org.mockito.ArgumentMatchers.any(ProfitAnalysisLambdaCommand.class);
  }

  private SurvivalAnalysisLambdaCommand anySurvivalLambdaCommand() {
    return org.mockito.ArgumentMatchers.any(SurvivalAnalysisLambdaCommand.class);
  }

  private Region region() {
    return Region.builder()
        .sido("Seoul")
        .sigungu("Gangnam-gu")
        .dong("Yeoksam-dong")
        .code("1168010100")
        .codeType(RegionCodeType.LEGAL_DONG)
        .build();
  }

  private Industry industry(String code, String name) {
    return Industry.builder()
        .industryCode(code)
        .name(name)
        .parentIndustry(null)
        .level(IndustryLevel.SMALL)
        .status(IndustryStatus.ACTIVE)
        .build();
  }
}
