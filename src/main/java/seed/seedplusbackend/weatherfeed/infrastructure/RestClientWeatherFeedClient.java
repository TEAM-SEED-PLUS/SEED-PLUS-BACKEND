package seed.seedplusbackend.weatherfeed.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.weatherfeed.application.port.WeatherFeedClient;

@Component
public class RestClientWeatherFeedClient implements WeatherFeedClient {

  private final RestClient restClient;
  private final FastApiWeatherFeedProperties properties;

  public RestClientWeatherFeedClient(
      @Qualifier("externalRestClientBuilder") RestClient.Builder restClientBuilder,
      FastApiWeatherFeedProperties properties) {
    this.restClient = restClientBuilder.build();
    this.properties = properties;
  }

  @Override
  public JsonNode getOverview(String date, String time, String timeBand) {
    UriComponentsBuilder builder = endpointBuilder("/api/v1/weather-feeds/overview");
    addOptionalQuery(builder, "date", date);
    addOptionalQuery(builder, "time", time);
    addOptionalQuery(builder, "time_band", timeBand);
    return get(builder.encode().build().toUri());
  }

  @Override
  public JsonNode getDetail(String district, String date, String time, String timeBand) {
    UriComponentsBuilder builder =
        endpointBuilder("/api/v1/weather-feeds").queryParam("district", district);
    addOptionalQuery(builder, "date", date);
    addOptionalQuery(builder, "time", time);
    addOptionalQuery(builder, "time_band", timeBand);
    return get(builder.encode().build().toUri());
  }

  private JsonNode get(URI uri) {
    try {
      JsonNode response =
          restClient
              .get()
              .uri(uri)
              .retrieve()
              .onStatus(HttpStatusCode::isError, this::throwWeatherFeedException)
              .body(JsonNode.class);
      if (response == null) {
        throw new ApplicationException(
            ErrorCode.WEATHER_FEED_API_REQUEST_FAILED, "empty response", null);
      }
      return response;
    } catch (ApplicationException exception) {
      throw exception;
    } catch (RestClientException exception) {
      throw new ApplicationException(ErrorCode.WEATHER_FEED_API_REQUEST_FAILED, exception);
    }
  }

  private void throwWeatherFeedException(
      org.springframework.http.HttpRequest request,
      org.springframework.http.client.ClientHttpResponse response) {
    try {
      throw new ApplicationException(
          ErrorCode.WEATHER_FEED_API_REQUEST_FAILED,
          "status=%d".formatted(response.getStatusCode().value()),
          null);
    } catch (java.io.IOException exception) {
      throw new ApplicationException(ErrorCode.WEATHER_FEED_API_REQUEST_FAILED, exception);
    }
  }

  private UriComponentsBuilder endpointBuilder(String path) {
    String baseUrl = properties.baseUrl().replaceAll("/+$", "");
    return UriComponentsBuilder.fromUriString(baseUrl + path);
  }

  private void addOptionalQuery(UriComponentsBuilder builder, String name, String value) {
    if (value != null && !value.isBlank()) {
      builder.queryParam(name, value);
    }
  }
}
