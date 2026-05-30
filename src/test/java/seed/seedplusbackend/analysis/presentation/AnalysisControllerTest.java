package seed.seedplusbackend.analysis.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import seed.seedplusbackend.analysis.application.AnalysisService;
import seed.seedplusbackend.analysis.application.command.ProfitAnalysisCommand;
import seed.seedplusbackend.analysis.application.command.SurvivalAnalysisCommand;
import seed.seedplusbackend.analysis.application.result.ProfitAnalysisResult;
import seed.seedplusbackend.analysis.application.result.SurvivalAnalysisResult;
import seed.seedplusbackend.global.error.GlobalExceptionHandler;
import seed.seedplusbackend.global.security.AuthenticatedUser;
import seed.seedplusbackend.user.domain.entity.UserRole;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalysisController")
class AnalysisControllerTest {

  private MockMvc mockMvc;

  @Mock private AnalysisService analysisService;

  @BeforeEach
  void setUp() {
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();
    mockMvc =
        MockMvcBuilders.standaloneSetup(new AnalysisController(analysisService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .setValidator(validator)
            .build();
  }

  @Test
  @DisplayName("Profit analysis returns typed full lambda response")
  void calculateProfit_returnsTypedResponse() throws Exception {
    given(analysisService.calculateProfit(eq(9L), any(ProfitAnalysisCommand.class)))
        .willReturn(profitResult());

    try {
      SecurityContextHolder.getContext().setAuthentication(userAuthentication());

      mockMvc
          .perform(
              post("/api/v1/analysis/profit")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(validProfitRequest()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value(200))
          .andExpect(jsonPath("$.data.input.industry").value("food"))
          .andExpect(jsonPath("$.data.assumptions.regionMultiplier").value(1.3))
          .andExpect(jsonPath("$.data.result.monthlyProfit").value(499));
    } finally {
      SecurityContextHolder.clearContext();
    }

    verify(analysisService)
        .calculateProfit(
            eq(9L),
            argThat(
                command ->
                    command != null
                        && "I561".equals(command.industryCode())
                        && "1168010100".equals(command.regionCode())
                        && new BigDecimal("30").compareTo(command.area()) == 0
                        && Integer.valueOf(3).equals(command.staff())));
  }

  @Test
  @DisplayName("Survival analysis returns typed full lambda response")
  void calculateSurvival_returnsTypedResponse() throws Exception {
    given(analysisService.calculateSurvival(eq(9L), any(SurvivalAnalysisCommand.class)))
        .willReturn(survivalResult());

    try {
      SecurityContextHolder.getContext().setAuthentication(userAuthentication());

      mockMvc
          .perform(
              post("/api/v1/analysis/survival")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(validSurvivalRequest()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value(200))
          .andExpect(jsonPath("$.data.input.startupType").value("transfer"))
          .andExpect(jsonPath("$.data.derived.estMonthlyRevenue").value(3360))
          .andExpect(jsonPath("$.data.scoreBreakdown.s6_rentBurden").value(-20))
          .andExpect(jsonPath("$.data.survival.survival1Year").value("40 ~ 55%"));
    } finally {
      SecurityContextHolder.clearContext();
    }

    verify(analysisService)
        .calculateSurvival(
            eq(9L),
            argThat(
                command ->
                    command != null
                        && "I562".equals(command.industryCode())
                        && "1168010100".equals(command.regionCode())
                        && "transfer".equals(command.startupType())
                        && new BigDecimal("4200").compareTo(command.avgSalesAmt()) == 0));
  }

  @Test
  @DisplayName("Profit analysis request requires non-blank industry code")
  void calculateProfit_returnsBadRequest_whenIndustryCodeIsBlank() throws Exception {
    try {
      SecurityContextHolder.getContext().setAuthentication(userAuthentication());

      mockMvc
          .perform(
              post("/api/v1/analysis/profit")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {
                        "industryCode": "   ",
                        "regionCode": "1168010100",
                        "area": 30,
                        "invest": 5000,
                        "rent": 300,
                        "premium": 2000,
                        "staff": 3
                      }
                      """))
          .andExpect(status().isBadRequest());
    } finally {
      SecurityContextHolder.clearContext();
    }

    verifyNoInteractions(analysisService);
  }

  @Test
  @DisplayName("Survival analysis request requires area")
  void calculateSurvival_returnsBadRequest_whenAreaMissing() throws Exception {
    try {
      SecurityContextHolder.getContext().setAuthentication(userAuthentication());

      mockMvc
          .perform(
              post("/api/v1/analysis/survival")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {
                        "regionCode": "1168010100",
                        "industryCode": "I562",
                        "rent": 250,
                        "deposit": 2000,
                        "avgSales": 4,
                        "salesGrowth": 3,
                        "density": 4,
                        "vacancy": 2,
                        "traffic": 4,
                        "churn": 2,
                        "startupType": "transfer",
                        "avgSalesAmt": 4200
                      }
                      """))
          .andExpect(status().isBadRequest());
    } finally {
      SecurityContextHolder.clearContext();
    }

    verifyNoInteractions(analysisService);
  }

  private UsernamePasswordAuthenticationToken userAuthentication() {
    AuthenticatedUser authenticatedUser =
        new AuthenticatedUser(9L, "01012345678", UserRole.GENERAL);
    return new UsernamePasswordAuthenticationToken(
        authenticatedUser, null, authenticatedUser.getAuthorities());
  }

  private String validProfitRequest() {
    return """
        {
          "industryCode": "I561",
          "regionCode": "1168010100",
          "area": 30,
          "invest": 5000,
          "rent": 300,
          "premium": 2000,
          "staff": 3
        }
        """;
  }

  private String validSurvivalRequest() {
    return """
        {
          "regionCode": "1168010100",
          "industryCode": "I562",
          "area": 40,
          "rent": 250,
          "deposit": 2000,
          "avgSales": 4,
          "salesGrowth": 3,
          "density": 4,
          "vacancy": 2,
          "traffic": 4,
          "churn": 2,
          "startupType": "transfer",
          "avgSalesAmt": 4200
        }
        """;
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
}
