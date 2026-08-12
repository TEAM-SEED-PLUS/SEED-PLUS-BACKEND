package seed.seedplusbackend.analysis.infrastructure;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
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
    UriComponentsBuilder builder =
        UriComponentsBuilder.fromUriString(properties.profit().endpoint())
            .queryParam("storeName", command.storeName())
            .queryParam("industry", command.industry())
            .queryParam("region", command.region())
            .queryParam("area", number(command.area()))
            .queryParam("invest", number(command.invest()))
            .queryParam("rent", number(command.rent()))
            .queryParam("premium", number(command.premium()))
            .queryParam("staff", command.staff());
    add(builder, "THSMON_SELNG_AMT", command.monthlySalesAmount());
    add(builder, "storeCountInTrdar", command.storeCountInCommercialArea());
    add(builder, "guAvgSalesAmt", command.districtAverageSalesAmount());
    add(builder, "cityAvgSalesAmt", command.cityAverageSalesAmount());
    add(builder, "storeZoneOne", command.storeZoneOne());
    add(builder, "storeListInArea", command.storeListInArea());
    add(builder, "storeListInRadius", command.storeListInRadius());
    add(builder, "competitorCount", command.competitorCount());
    builder.queryParam("fallbackUsed", command.fallbackUsed());
    addSources(builder, command.dataSources());
    return builder.encode().build().toUri();
  }

  private URI survivalUri(SurvivalAnalysisLambdaCommand command) {
    UriComponentsBuilder builder =
        UriComponentsBuilder.fromUriString(properties.survival().endpoint())
            .queryParam("storeName", command.storeName())
            .queryParam("industry", command.industry())
            .queryParam("region", command.region())
            .queryParam("area", number(command.area()))
            .queryParam("invest", number(command.invest()))
            .queryParam("rent", number(command.rent()))
            .queryParam("premium", number(command.premium()))
            .queryParam("staff", command.staff())
            .queryParam("startupType", "new");
    add(builder, "THSMON_SELNG_AMT", command.monthlySalesAmount());
    add(builder, "storeCountInTrdar", command.storeCountInCommercialArea());
    add(builder, "salesGrowthRate", command.salesGrowthRate());
    add(builder, "storeDensity", command.storeDensity());
    add(builder, "vacancyRate", command.vacancyRate());
    add(builder, "trafficIndex", command.trafficIndex());
    add(builder, "survivalRate", command.survivalRate());
    add(builder, "closedBusinesses", command.closedBusinesses());
    add(builder, "activeBusinesses", command.activeBusinesses());
    add(builder, "newBusinesses", command.newBusinesses());
    builder.queryParam("fallbackUsed", command.fallbackUsed());
    addSources(builder, command.dataSources());
    return builder.encode().build().toUri();
  }

  private void add(UriComponentsBuilder builder, String name, Object value) {
    if (value != null) builder.queryParam(name, value);
  }

  private void addSources(UriComponentsBuilder builder, List<String> sources) {
    if (sources == null || sources.isEmpty()) return;
    String json =
        sources.stream()
            .map(value -> "\"" + value.replace("\"", "\\\"") + "\"")
            .collect(java.util.stream.Collectors.joining(",", "[", "]"));
    builder.queryParam("dataSources", json);
  }

  private String number(BigDecimal value) {
    return value.stripTrailingZeros().toPlainString();
  }
}
