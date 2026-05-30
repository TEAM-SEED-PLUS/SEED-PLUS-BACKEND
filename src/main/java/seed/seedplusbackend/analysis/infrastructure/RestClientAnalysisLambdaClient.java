package seed.seedplusbackend.analysis.infrastructure;

import java.math.BigDecimal;
import java.net.URI;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import seed.seedplusbackend.analysis.application.command.ProfitAnalysisLambdaCommand;
import seed.seedplusbackend.analysis.application.command.SurvivalAnalysisLambdaCommand;
import seed.seedplusbackend.analysis.application.port.AnalysisLambdaClient;
import seed.seedplusbackend.analysis.application.result.ProfitAnalysisResult;
import seed.seedplusbackend.analysis.application.result.SurvivalAnalysisResult;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@Component
public class RestClientAnalysisLambdaClient implements AnalysisLambdaClient {

  private static final String API_KEY_HEADER = "x-api-key";

  private final RestClient restClient;
  private final AnalysisLambdaProperties properties;

  public RestClientAnalysisLambdaClient(
      @Qualifier("externalRestClientBuilder") RestClient.Builder restClientBuilder,
      AnalysisLambdaProperties properties) {
    this.restClient = restClientBuilder.build();
    this.properties = properties;
  }

  @Override
  public ProfitAnalysisResult requestProfit(ProfitAnalysisLambdaCommand command) {
    return get(profitUri(command), properties.profit().apiKey(), ProfitAnalysisResult.class);
  }

  @Override
  public SurvivalAnalysisResult requestSurvival(SurvivalAnalysisLambdaCommand command) {
    return get(survivalUri(command), properties.survival().apiKey(), SurvivalAnalysisResult.class);
  }

  private <T> T get(URI uri, String apiKey, Class<T> responseType) {
    try {
      T response =
          restClient
              .get()
              .uri(uri)
              .header(API_KEY_HEADER, apiKey)
              .retrieve()
              .onStatus(HttpStatusCode::isError, this::throwAnalysisFunctionException)
              .body(responseType);
      if (response == null) {
        throw new ApplicationException(
            ErrorCode.ANALYSIS_FUNCTION_CALL_FAILED, "empty response", null);
      }
      return response;
    } catch (ApplicationException e) {
      throw e;
    } catch (RestClientException e) {
      throw new ApplicationException(ErrorCode.ANALYSIS_FUNCTION_CALL_FAILED, e);
    }
  }

  private void throwAnalysisFunctionException(
      org.springframework.http.HttpRequest request,
      org.springframework.http.client.ClientHttpResponse response) {
    try {
      throw new ApplicationException(
          ErrorCode.ANALYSIS_FUNCTION_CALL_FAILED,
          "status=%d".formatted(response.getStatusCode().value()),
          null);
    } catch (java.io.IOException e) {
      throw new ApplicationException(ErrorCode.ANALYSIS_FUNCTION_CALL_FAILED, e);
    }
  }

  private URI profitUri(ProfitAnalysisLambdaCommand command) {
    return UriComponentsBuilder.fromUriString(properties.profit().endpoint())
        .queryParam("industry", command.industry())
        .queryParam("region", command.region())
        .queryParam("area", number(command.area()))
        .queryParam("invest", number(command.invest()))
        .queryParam("rent", number(command.rent()))
        .queryParam("premium", number(command.premium()))
        .queryParam("staff", command.staff())
        .encode()
        .build()
        .toUri();
  }

  private URI survivalUri(SurvivalAnalysisLambdaCommand command) {
    return UriComponentsBuilder.fromUriString(properties.survival().endpoint())
        .queryParam("region", command.region())
        .queryParam("industry", command.industry())
        .queryParam("area", number(command.area()))
        .queryParam("rent", number(command.rent()))
        .queryParam("deposit", number(command.deposit()))
        .queryParam("avgSales", number(command.avgSales()))
        .queryParam("salesGrowth", number(command.salesGrowth()))
        .queryParam("density", number(command.density()))
        .queryParam("vacancy", number(command.vacancy()))
        .queryParam("traffic", number(command.traffic()))
        .queryParam("churn", number(command.churn()))
        .queryParam("startupType", command.startupType())
        .queryParam("avgSalesAmt", number(command.avgSalesAmt()))
        .encode()
        .build()
        .toUri();
  }

  private String number(BigDecimal value) {
    return value.stripTrailingZeros().toPlainString();
  }
}
