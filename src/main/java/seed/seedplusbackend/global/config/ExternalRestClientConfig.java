package seed.seedplusbackend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class ExternalRestClientConfig {

  @Bean
  public RestClient.Builder externalRestClientBuilder(
      @Value("${external.rest-client.connect-timeout-millis:3000}") int connectTimeoutMillis,
      @Value("${external.rest-client.read-timeout-millis:5000}") int readTimeoutMillis) {
    return restClientBuilder(connectTimeoutMillis, readTimeoutMillis);
  }

  @Bean
  public RestClient.Builder kosisBusinessSurvivalRestClientBuilder(
      @Value("${kosis.business-survival.open-api.connect-timeout-millis:3000}")
          int connectTimeoutMillis,
      @Value("${kosis.business-survival.open-api.read-timeout-millis:30000}")
          int readTimeoutMillis) {
    return restClientBuilder(connectTimeoutMillis, readTimeoutMillis);
  }

  private RestClient.Builder restClientBuilder(int connectTimeoutMillis, int readTimeoutMillis) {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(connectTimeoutMillis);
    requestFactory.setReadTimeout(readTimeoutMillis);

    return RestClient.builder().requestFactory(requestFactory);
  }
}
