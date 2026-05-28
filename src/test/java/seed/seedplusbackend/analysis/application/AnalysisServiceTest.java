package seed.seedplusbackend.analysis.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
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
import seed.seedplusbackend.analysis.application.result.ProfitAnalysisResult;
import seed.seedplusbackend.analysis.application.result.SurvivalAnalysisResult;
import seed.seedplusbackend.global.cache.CaffeineCacheStore;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.industry.domain.entity.IndustryLevel;
import seed.seedplusbackend.industry.domain.entity.IndustryStatus;
import seed.seedplusbackend.industry.domain.repository.IndustryRepository;
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

  @BeforeEach
  void setUp() {
    analysisService =
        new AnalysisService(
            analysisLambdaClient, new CaffeineCacheStore(), regionRepository, industryRepository);
  }

  @Test
  @DisplayName("Profit analysis caches successful lambda responses by normalized input")
  void calculateProfit_returnsCachedResult_whenSameInputRequestedAgain() {
    ProfitAnalysisCommand firstCommand =
        new ProfitAnalysisCommand(
            " I561 ",
            "1168010100",
            new BigDecimal("30.0"),
            new BigDecimal("5000"),
            new BigDecimal("300"),
            new BigDecimal("2000"),
            3);
    ProfitAnalysisCommand secondCommand =
        new ProfitAnalysisCommand(
            "I561",
            "1168010100",
            new BigDecimal("30"),
            new BigDecimal("5000.0"),
            new BigDecimal("300.00"),
            new BigDecimal("2000"),
            3);
    ProfitAnalysisResult result = profitResult();
    given(regionRepository.findByCode("1168010100")).willReturn(java.util.Optional.of(region()));
    given(industryRepository.findByIndustryCodeAndStatus("I561", IndustryStatus.ACTIVE))
        .willReturn(java.util.Optional.of(industry("I561", "Cafe")));
    given(analysisLambdaClient.requestProfit(anyProfitLambdaCommand())).willReturn(result);

    ProfitAnalysisResult first = analysisService.calculateProfit(1L, firstCommand);
    ProfitAnalysisResult second = analysisService.calculateProfit(2L, secondCommand);

    assertThat(first).isSameAs(result);
    assertThat(second).isSameAs(result);
    verify(analysisLambdaClient, times(1))
        .requestProfit(
            org.mockito.ArgumentMatchers.argThat(
                command ->
                    command != null
                        && "Cafe".equals(command.industry())
                        && "Seoul Gangnam-gu Yeoksam-dong".equals(command.region())));
  }

  @Test
  @DisplayName("Survival analysis caches successful lambda responses by normalized input")
  void calculateSurvival_returnsCachedResult_whenSameInputRequestedAgain() {
    SurvivalAnalysisCommand command = survivalCommand();
    SurvivalAnalysisResult result = survivalResult();
    given(regionRepository.findByCode("1168010100")).willReturn(java.util.Optional.of(region()));
    given(industryRepository.findByIndustryCodeAndStatus("I562", IndustryStatus.ACTIVE))
        .willReturn(java.util.Optional.of(industry("I562", "Restaurant")));
    given(analysisLambdaClient.requestSurvival(anySurvivalLambdaCommand())).willReturn(result);

    SurvivalAnalysisResult first = analysisService.calculateSurvival(1L, command);
    SurvivalAnalysisResult second = analysisService.calculateSurvival(1L, command);

    assertThat(first).isSameAs(result);
    assertThat(second).isSameAs(result);
    verify(analysisLambdaClient, times(1))
        .requestSurvival(
            org.mockito.ArgumentMatchers.argThat(
                lambdaCommand ->
                    lambdaCommand != null
                        && "Restaurant".equals(lambdaCommand.industry())
                        && "Seoul Gangnam-gu Yeoksam-dong".equals(lambdaCommand.region())));
  }

  @Test
  @DisplayName("Lambda failures are not cached")
  void calculateProfit_doesNotCacheFailure() {
    ProfitAnalysisCommand command =
        new ProfitAnalysisCommand(
            "I561",
            "1168010100",
            new BigDecimal("30"),
            new BigDecimal("5000"),
            new BigDecimal("300"),
            new BigDecimal("2000"),
            3);
    ProfitAnalysisResult result = profitResult();
    given(regionRepository.findByCode("1168010100")).willReturn(java.util.Optional.of(region()));
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

  private SurvivalAnalysisCommand survivalCommand() {
    return new SurvivalAnalysisCommand(
        "1168010100",
        "I562",
        new BigDecimal("40"),
        new BigDecimal("250"),
        new BigDecimal("2000"),
        new BigDecimal("4"),
        new BigDecimal("3"),
        new BigDecimal("4"),
        new BigDecimal("2"),
        new BigDecimal("4"),
        new BigDecimal("2"),
        "transfer",
        new BigDecimal("4200"));
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
