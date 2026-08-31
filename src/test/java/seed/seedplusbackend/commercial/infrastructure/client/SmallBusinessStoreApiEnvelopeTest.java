package seed.seedplusbackend.commercial.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("소상공인 상가정보 API 응답")
class SmallBusinessStoreApiEnvelopeTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @DisplayName("response로 감싸진 JSON 응답을 변환한다")
  void deserialize_unwrapsResponse() throws Exception {
    String json =
        """
        {
          "response": {
            "header": {
              "resultCode": "00",
              "resultMsg": "NORMAL SERVICE"
            },
            "body": {
              "items": [{
                "bizesId": "19911025",
                "bizesNm": "테스트 상가",
                "indsLclsCd": "Q",
                "lon": 127.01,
                "lat": 37.51
              }],
              "numOfRows": 1000,
              "pageNo": 1,
              "totalCount": 1
            }
          }
        }
        """;

    SmallBusinessStoreApiEnvelope envelope =
        objectMapper.readValue(json, SmallBusinessStoreApiEnvelope.class);
    SmallBusinessStoreApiResponse response = envelope.unwrap();

    assertThat(response.header().resultCode()).isEqualTo("00");
    assertThat(response.body().totalCount()).isEqualTo(1);
    assertThat(response.body().items()).hasSize(1);
    assertThat(response.body().items().getFirst().bizesId()).isEqualTo("19911025");
  }

  @Test
  @DisplayName("조회 결과가 없는 응답도 header를 변환한다")
  void deserialize_unwrapsNoDataResponse() throws Exception {
    String json =
        """
        {
          "response": {
            "header": {
              "resultCode": "03",
              "resultMsg": "NODATA_ERROR"
            }
          }
        }
        """;

    SmallBusinessStoreApiEnvelope envelope =
        objectMapper.readValue(json, SmallBusinessStoreApiEnvelope.class);

    assertThat(envelope.unwrap().header().resultCode()).isEqualTo("03");
    assertThat(envelope.unwrap().body()).isNull();
  }
}
