package seed.seedplusbackend.payment.infrastructure.gmpay;

import java.io.IOException;
import java.net.URI;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

abstract class GmpayRestClientSupport {

  protected final GmpayProperties properties;
  protected final RestClient restClient;

  protected GmpayRestClientSupport(
      @Qualifier("externalRestClientBuilder") RestClient.Builder restClientBuilder,
      GmpayProperties properties) {
    this.properties = properties;
    this.restClient = restClientBuilder.build();
  }

  protected URI uri(String path) {
    return UriComponentsBuilder.fromUriString(baseUrl() + path).build().toUri();
  }

  protected URI uri(String path, String queryParameterName, String queryParameterValue) {
    return UriComponentsBuilder.fromUriString(baseUrl() + path)
        .queryParam(queryParameterName, queryParameterValue)
        .build()
        .toUri();
  }

  protected void validateRequired(ErrorCode errorCode, String... values) {
    for (String value : values) {
      if (isBlank(value)) {
        throw new ApplicationException(errorCode, "required properties are missing", null);
      }
    }
  }

  protected void throwGmpayException(ErrorCode errorCode, ClientHttpResponse response) {
    try {
      throw new ApplicationException(
          errorCode, "status=%d".formatted(response.getStatusCode().value()), null);
    } catch (IOException exception) {
      throw new ApplicationException(errorCode, exception);
    }
  }

  protected boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private String baseUrl() {
    return properties.baseUrl().replaceAll("/+$", "");
  }
}
