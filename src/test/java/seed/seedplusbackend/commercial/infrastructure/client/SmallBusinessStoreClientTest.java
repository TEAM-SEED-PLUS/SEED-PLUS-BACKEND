package seed.seedplusbackend.commercial.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.DefaultUriBuilderFactory;
import seed.seedplusbackend.commercial.application.command.SmallBusinessStoreCollectCommand;

@DisplayName("소상공인 상가정보 API 클라이언트")
class SmallBusinessStoreClientTest {

  @Test
  @DisplayName("4xx 응답은 재시도하지 않는 요청 실패로 변환한다")
  void requestException_doesNotRetryClientError() {
    assertThat(SmallBusinessStoreClient.requestException(HttpStatus.UNAUTHORIZED).isRetryable())
        .isFalse();
  }

  @Test
  @DisplayName("5xx 응답은 재시도 가능한 요청 실패로 변환한다")
  void requestException_retriesServerError() {
    assertThat(
            SmallBusinessStoreClient.requestException(HttpStatus.SERVICE_UNAVAILABLE).isRetryable())
        .isTrue();
  }

  @Test
  @DisplayName("공공데이터포털 인코딩 인증키를 한 번 복원한다")
  void decodeServiceKey_decodesEncodedKey() {
    String encodedKey = "abc%2Bdef%2Fghi%3D%3D";

    String decodedKey = SmallBusinessStoreClient.decodeServiceKey(encodedKey);

    assertThat(decodedKey).isEqualTo("abc+def/ghi==");
  }

  @Test
  @DisplayName("디코딩 인증키는 그대로 사용한다")
  void decodeServiceKey_keepsDecodedKey() {
    String decodedKey = "abc+def/ghi==";

    assertThat(SmallBusinessStoreClient.decodeServiceKey(decodedKey)).isEqualTo(decodedKey);
  }

  @Test
  @DisplayName("인증키의 +를 퍼센트 인코딩해 요청 URI를 만든다")
  void buildUri_encodesPlusInServiceKey() {
    SmallBusinessStoreClient client = client("abc%2Bdef%2Fghi%3D%3D");

    URI uri =
        client.buildUri(
            new DefaultUriBuilderFactory("https://example.com").builder(),
            new SmallBusinessStoreCollectCommand("A001", null, null, null, false),
            1,
            100);

    assertThat(uri.getRawQuery()).contains("serviceKey=abc%2Bdef%2Fghi%3D%3D");
  }

  @Test
  @DisplayName("디코딩된 인증키의 +도 퍼센트 인코딩해 요청 URI를 만든다")
  void buildUri_encodesPlusInDecodedServiceKey() {
    SmallBusinessStoreClient client = client("abc+def/ghi==");

    URI uri =
        client.buildUri(
            new DefaultUriBuilderFactory("https://example.com").builder(),
            new SmallBusinessStoreCollectCommand("A001", null, null, null, false),
            1,
            100);

    assertThat(uri.getRawQuery()).contains("serviceKey=abc%2Bdef%2Fghi%3D%3D");
  }

  private SmallBusinessStoreClient client(String serviceKey) {
    return new SmallBusinessStoreClient(
        new SmallBusinessStoreOpenApiProperties(
            serviceKey, "https://example.com", "stores", "json", 100, 100, 3));
  }
}
