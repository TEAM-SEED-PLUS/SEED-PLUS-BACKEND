package seed.seedplusbackend.analysis.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import seed.seedplusbackend.analysis.application.command.ProfitAnalysisLambdaCommand;
import seed.seedplusbackend.analysis.application.command.SurvivalAnalysisLambdaCommand;
import seed.seedplusbackend.analysis.application.result.ProfitAnalysisResult;
import seed.seedplusbackend.analysis.application.result.SurvivalAnalysisResult;
import seed.seedplusbackend.global.error.ApplicationException;

@DisplayName("RestClientAnalysisLambdaClient")
class RestClientAnalysisLambdaClientTest {

  private RestClientAnalysisLambdaClient client;
  private MockRestServiceServer server;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    AnalysisLambdaProperties properties =
        new AnalysisLambdaProperties(
            new AnalysisLambdaProperties.FunctionProperties(
                "https://profit.example/dev/", "profit-key"),
            new AnalysisLambdaProperties.FunctionProperties(
                "https://survival.example/dev/", "survival-key"));
    client = new RestClientAnalysisLambdaClient(builder, properties);
  }

  @Test
  @DisplayName("Profit lambda request maps query params and api key header")
  void requestProfit_mapsRequestAndResponse() {
    server
        .expect(
            once(),
            requestTo(
                "https://profit.example/dev/profit?storeName=star&industry=food&region=gangnam&area=30"
                    + "&invest=5000&rent=300&premium=2000&staff=3&THSMON_SELNG_AMT=3120000000"
                    + "&storeCountInTrdar=104&guAvgSalesAmt=3500000&cityAvgSalesAmt=2900000"
                    + "&storeZoneOne=100&storeListInArea=300&storeListInRadius=50"
                    + "&competitorCount=18&fallbackUsed=false&dataSources=%5B%22source%22%5D"))
        .andExpect(header("x-api-key", "profit-key"))
        .andRespond(withSuccess(profitResponseJson(), MediaType.APPLICATION_JSON));

    ProfitAnalysisResult result = client.requestProfit(profitCommand());

    assertThat(result.result().monthlyProfit()).isEqualByComparingTo(new BigDecimal("499"));
    assertThat(result.assumptions().regionMultiplier()).isEqualByComparingTo("1.3");
    server.verify();
  }

  @Test
  @DisplayName("Survival lambda request maps query params and api key header")
  void requestSurvival_mapsRequestAndResponse() {
    server
        .expect(
            once(),
            requestTo(
                "https://survival.example/dev/survival?storeName=star&industry=cafe&region=gangnam&area=40"
                    + "&invest=8000&rent=250&premium=2000&staff=3&startupType=new"
                    + "&THSMON_SELNG_AMT=3120000000&storeCountInTrdar=104&salesGrowthRate=5.2"
                    + "&storeDensity=42&vacancyRate=8.0&trafficIndex=14000&survivalRate=68"
                    + "&closedBusinesses=120&activeBusinesses=1500&newBusinesses=180"
                    + "&fallbackUsed=true&dataSources=%5B%22source%22%5D"))
        .andExpect(header("x-api-key", "survival-key"))
        .andRespond(withSuccess(survivalResponseJson(), MediaType.APPLICATION_JSON));

    SurvivalAnalysisResult result = client.requestSurvival(survivalCommand());

    assertThat(result.survival().survival1Year()).isEqualTo("40 ~ 55%");
    assertThat(result.scoreBreakdown().s6_rentBurden()).isEqualByComparingTo("-20");
    server.verify();
  }

  @Test
  @DisplayName("Non-2xx lambda responses are wrapped as application exceptions")
  void requestProfit_throwsApplicationException_whenLambdaReturnsError() {
    server
        .expect(
            once(),
            requestTo(
                "https://profit.example/dev/profit?storeName=star&industry=food&region=gangnam&area=30"
                    + "&invest=5000&rent=300&premium=2000&staff=3&THSMON_SELNG_AMT=3120000000"
                    + "&storeCountInTrdar=104&guAvgSalesAmt=3500000&cityAvgSalesAmt=2900000"
                    + "&storeZoneOne=100&storeListInArea=300&storeListInRadius=50"
                    + "&competitorCount=18&fallbackUsed=false&dataSources=%5B%22source%22%5D"))
        .andExpect(header("x-api-key", "profit-key"))
        .andRespond(withStatus(HttpStatus.BAD_GATEWAY));

    assertThatThrownBy(() -> client.requestProfit(profitCommand()))
        .isInstanceOf(ApplicationException.class);

    server.verify();
  }

  private SurvivalAnalysisLambdaCommand survivalCommand() {
    return new SurvivalAnalysisLambdaCommand(
        "star",
        "cafe",
        "gangnam",
        new BigDecimal("40"),
        new BigDecimal("8000"),
        new BigDecimal("250"),
        new BigDecimal("2000"),
        3,
        3120000000L,
        104,
        new BigDecimal("5.2"),
        42,
        new BigDecimal("8.0"),
        14000,
        new BigDecimal("68"),
        new BigDecimal("120"),
        new BigDecimal("1500"),
        new BigDecimal("180"),
        true,
        java.util.List.of("source"));
  }

  private ProfitAnalysisLambdaCommand profitCommand() {
    return new ProfitAnalysisLambdaCommand(
        "star",
        "food",
        "gangnam",
        new BigDecimal("30"),
        new BigDecimal("5000"),
        new BigDecimal("300"),
        new BigDecimal("2000"),
        3,
        3120000000L,
        104,
        new BigDecimal("3500000"),
        new BigDecimal("2900000"),
        100,
        300,
        50,
        18,
        false,
        java.util.List.of("source"));
  }

  private String profitResponseJson() {
    return """
        {
          "input": {
            "industry": "food",
            "region": "gangnam",
            "area": 30.0,
            "invest": 5000.0,
            "rent": 300.0,
            "premium": 2000.0,
            "staff": 3
          },
          "assumptions": {
            "baseRevenue": 80,
            "regionMultiplier": 1.3,
            "baseProfitRate": 18,
            "variableCostRate": 42,
            "fixedOverheadRate": 8,
            "staffCostPerPerson": 250
          },
          "result": {
            "monthlyRev": 3120,
            "staffCost": 750,
            "fixedOverheadCost": 250,
            "fixedCost": 1300.0,
            "variableCost": 1310,
            "staffImpact": 24,
            "rentImpact": 10,
            "variableImpact": 42,
            "fixedOverheadImpact": 8,
            "profitRate": 16,
            "monthlyProfit": 499,
            "totalInvest": 7000.0,
            "paybackMonths": 14,
            "propertyScore": 81
          }
        }
        """;
  }

  private String survivalResponseJson() {
    return """
        {
          "input": {
            "region": "gangnam",
            "industry": "cafe",
            "area": 40.0,
            "rent": 250.0,
            "deposit": 2000.0,
            "avgSales": 4.0,
            "salesGrowth": 3.0,
            "density": 4.0,
            "vacancy": 2.0,
            "traffic": 4.0,
            "churn": 2.0,
            "startupType": "transfer",
            "avgSalesAmt": 4200.0
          },
          "derived": {
            "estMonthlyRevenue": 3360,
            "competitionRatio": 80,
            "rentBurden": 74,
            "vitalityScore": 62,
            "stabilityIndex": 75
          },
          "scoreBreakdown": {
            "s1_salesStability": 15,
            "s2_salesGrowth": 2,
            "s3_competition": -12,
            "s4_vacancyRisk": 0,
            "s5_traffic": 8,
            "s6_rentBurden": -20,
            "s7_churn": 1,
            "s8_startupTypeBonus": 5,
            "rawScore": 49,
            "totalScore": 49
          },
          "survival": {
            "grade": "caution",
            "survival1Year": "40 ~ 55%",
            "survival3Year": "20 ~ 35%"
          }
        }
        """;
  }
}
