package seed.seedplusbackend.commercial.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import seed.seedplusbackend.commercial.application.result.SeoulSdotFootTrafficPageResult;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

class SeoulSdotFootTrafficClientTest {

  @Test
  void fetch_parsesUtf8ResponseAndSkipsInvalidRows() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    SeoulSdotFootTrafficClient client = client(builder);
    server
        .expect(requestTo("http://openapi.seoul.go.kr:8088/test-key/json/IotVdata018/1/1000"))
        .andRespond(
            withSuccess(
                """
                {
                  "IotVdata018": {
                    "list_total_count": 2,
                    "RESULT": {"CODE": "INFO-000", "MESSAGE": "정상 처리되었습니다"},
                    "row": [
                      {
                        "MODEL_NM": "SDOT001",
                        "SERIAL_NO": "00000004093",
                        "SENSING_TIME": "2026-06-13_23:47:00",
                        "REGION": "residential_area",
                        "AUTONOMOUS_DISTRICT": "동작구",
                        "ADMINISTRATIVE_DISTRICT": "상도1동",
                        "VISITOR_COUNT": "16",
                        "REG_DTTM": "2026-06-13 23:58:03"
                      },
                      {
                        "SERIAL_NO": "invalid",
                        "SENSING_TIME": "invalid-time",
                        "VISITOR_COUNT": "-"
                      }
                    ]
                  }
                }
                """
                    .getBytes(StandardCharsets.UTF_8),
                MediaType.APPLICATION_JSON));

    SeoulSdotFootTrafficPageResult result = client.fetch(1, 1000);

    assertThat(result.totalCount()).isEqualTo(2);
    assertThat(result.rows()).hasSize(1);
    assertThat(result.rows().getFirst().autonomousDistrict()).isEqualTo("동작구");
    assertThat(result.rows().getFirst().administrativeDistrict()).isEqualTo("상도1동");
    assertThat(result.rows().getFirst().visitorCount()).isEqualTo(16);
    assertThat(result.rows().getFirst().sensingTime())
        .isEqualTo(LocalDateTime.of(2026, 6, 13, 23, 47));
    server.verify();
  }

  @Test
  void fetch_rejectsSeoulApiErrorResponse() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    SeoulSdotFootTrafficClient client = client(builder);
    server
        .expect(requestTo("http://openapi.seoul.go.kr:8088/test-key/json/IotVdata018/1/1000"))
        .andRespond(
            withSuccess(
                """
                {
                  "IotVdata018": {
                    "RESULT": {"CODE": "ERROR-300", "MESSAGE": "필수 값이 누락되어 있습니다"}
                  }
                }
                """
                    .getBytes(StandardCharsets.UTF_8),
                MediaType.APPLICATION_JSON));

    assertThatThrownBy(() -> client.fetch(1, 1000))
        .isInstanceOfSatisfying(
            ApplicationException.class,
            exception ->
                assertThat(exception.getErrorCode())
                    .isEqualTo(ErrorCode.SEOUL_OPEN_API_REQUEST_FAILED));
    server.verify();
  }

  @Test
  void fetch_rejectsTopLevelSeoulApiErrorResponse() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    SeoulSdotFootTrafficClient client = client(builder);
    server
        .expect(requestTo("http://openapi.seoul.go.kr:8088/test-key/json/IotVdata018/1/1000"))
        .andRespond(
            withSuccess(
                """
                {"RESULT":{"CODE":"ERROR-301","MESSAGE":"인증키가 유효하지 않습니다"}}
                """
                    .getBytes(StandardCharsets.UTF_8),
                MediaType.APPLICATION_JSON));

    assertThatThrownBy(() -> client.fetch(1, 1000))
        .isInstanceOfSatisfying(
            ApplicationException.class,
            exception ->
                assertThat(exception.getErrorCode())
                    .isEqualTo(ErrorCode.SEOUL_OPEN_API_REQUEST_FAILED));
    server.verify();
  }

  private SeoulSdotFootTrafficClient client(RestClient.Builder builder) {
    SeoulSdotOpenApiProperties properties =
        new SeoulSdotOpenApiProperties(
            "test-key", "http://openapi.seoul.go.kr:8088", "IotVdata018", "json", 1000, 0, 0, 0);
    return new SeoulSdotFootTrafficClient(builder, new ObjectMapper(), properties);
  }
}
