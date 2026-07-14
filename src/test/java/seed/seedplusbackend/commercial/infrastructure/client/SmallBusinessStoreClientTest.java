package seed.seedplusbackend.commercial.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("소상공인 상가정보 API 클라이언트")
class SmallBusinessStoreClientTest {

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
}
