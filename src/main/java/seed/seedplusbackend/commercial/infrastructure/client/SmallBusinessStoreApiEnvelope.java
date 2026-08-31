package seed.seedplusbackend.commercial.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SmallBusinessStoreApiEnvelope(
    SmallBusinessStoreApiResponse response,
    SmallBusinessStoreApiResponse.Header header,
    SmallBusinessStoreApiResponse.Body body) {

  public SmallBusinessStoreApiResponse unwrap() {
    return response != null ? response : new SmallBusinessStoreApiResponse(header, body);
  }
}
