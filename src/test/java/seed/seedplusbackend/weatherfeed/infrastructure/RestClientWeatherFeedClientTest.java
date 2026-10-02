package seed.seedplusbackend.weatherfeed.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import seed.seedplusbackend.global.error.ApplicationException;

@DisplayName("상권 날씨 FastAPI 클라이언트")
class RestClientWeatherFeedClientTest {

  private RestClientWeatherFeedClient client;
  private MockRestServiceServer server;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    client =
        new RestClientWeatherFeedClient(
            builder, new FastApiWeatherFeedProperties("http://127.0.0.1:8000/"));
  }

  @Test
  @DisplayName("요약 조회 시 입력된 선택 파라미터만 FastAPI에 전달한다")
  void getOverview_forwardsOptionalQueryParameters() {
    server
        .expect(
            once(),
            requestTo(Matchers.startsWith("http://127.0.0.1:8000/api/v1/weather-feeds/overview?")))
        .andExpect(queryParam("time_band", "%EC%95%84%EC%B9%A8"))
        .andRespond(
            withSuccess("{\"status\":\"ok\",\"districts\":[]}", MediaType.APPLICATION_JSON));

    JsonNode response = client.getOverview(null, null, "아침");

    assertThat(response.path("status").asText()).isEqualTo("ok");
    server.verify();
  }

  @Test
  @DisplayName("상세 조회 시 자치구와 시간대를 FastAPI에 전달한다")
  void getDetail_forwardsDistrictAndTimeBand() {
    server
        .expect(
            once(), requestTo(Matchers.startsWith("http://127.0.0.1:8000/api/v1/weather-feeds?")))
        .andExpect(queryParam("district", "%EA%B0%95%EB%82%A8%EA%B5%AC"))
        .andExpect(queryParam("time_band", "%EC%A0%80%EB%85%81"))
        .andRespond(
            withSuccess(
                "{\"opportunity_score\":64,\"content\":{\"items\":[]}}",
                MediaType.APPLICATION_JSON));

    JsonNode response = client.getDetail("강남구", null, null, "저녁");

    assertThat(response.path("opportunity_score").asInt()).isEqualTo(64);
    server.verify();
  }

  @Test
  @DisplayName("FastAPI 오류 응답을 애플리케이션 예외로 변환한다")
  void getDetail_throwsApplicationException_whenFastApiReturnsError() {
    server
        .expect(
            once(), requestTo(Matchers.startsWith("http://127.0.0.1:8000/api/v1/weather-feeds?")))
        .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

    assertThatThrownBy(() -> client.getDetail("강남구", null, null, null))
        .isInstanceOf(ApplicationException.class);

    server.verify();
  }
}
