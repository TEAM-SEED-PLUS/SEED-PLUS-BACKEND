package seed.seedplusbackend.commercial.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import seed.seedplusbackend.commercial.application.result.SeoulRealtimeCityPopulationResult;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

class SeoulRealtimeCityPopulationClientTest {

  @Test
  void fetch_parsesRealtimePopulationXml() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    SeoulRealtimeCityPopulationClient client = client(builder);
    server
        .expect(requestTo("http://openapi.seoul.go.kr:8088/test-key/xml/citydata_ppltn/1/5/POI009"))
        .andRespond(
            withSuccess(sampleXml().getBytes(StandardCharsets.UTF_8), MediaType.APPLICATION_XML));

    SeoulRealtimeCityPopulationResult result = client.fetch("POI009");

    assertThat(result.areaCode()).isEqualTo("POI009");
    assertThat(result.areaName()).isEqualTo("광화문·덕수궁");
    assertThat(result.populationMin()).isEqualTo(18000);
    assertThat(result.populationMax()).isEqualTo(20000);
    assertThat(result.estimatedPopulation()).isEqualTo(19000);
    assertThat(result.malePopulationRate()).isEqualByComparingTo("46.1");
    assertThat(result.replacementUsed()).isFalse();
    assertThat(result.populationTime()).isEqualTo(LocalDateTime.of(2026, 6, 14, 18, 15));
    server.verify();
  }

  @Test
  void fetch_rejectsSeoulErrorXml() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    SeoulRealtimeCityPopulationClient client = client(builder);
    server
        .expect(requestTo("http://openapi.seoul.go.kr:8088/test-key/xml/citydata_ppltn/1/5/POI999"))
        .andRespond(
            withSuccess(
                "<RESULT><CODE>ERROR-300</CODE><MESSAGE>잘못된 요청입니다.</MESSAGE></RESULT>"
                    .getBytes(StandardCharsets.UTF_8),
                MediaType.APPLICATION_XML));

    assertThatThrownBy(() -> client.fetch("POI999"))
        .isInstanceOfSatisfying(
            ApplicationException.class,
            exception ->
                assertThat(exception.getErrorCode())
                    .isEqualTo(ErrorCode.SEOUL_OPEN_API_REQUEST_FAILED));
    server.verify();
  }

  private SeoulRealtimeCityPopulationClient client(RestClient.Builder builder) {
    return new SeoulRealtimeCityPopulationClient(
        builder,
        new SeoulRealtimeCityOpenApiProperties(
            "test-key",
            "http://openapi.seoul.go.kr:8088",
            "citydata_ppltn",
            "xml",
            1,
            5,
            0,
            0,
            0,
            0));
  }

  private String sampleXml() {
    return """
        <?xml version="1.0" encoding="UTF-8"?>
        <SeoulRtd.citydata>
          <RESULT><CODE>INFO-000</CODE><MESSAGE>정상 처리되었습니다.</MESSAGE></RESULT>
          <CITYDATA>
            <AREA_NM>광화문·덕수궁</AREA_NM>
            <AREA_CD>POI009</AREA_CD>
            <LIVE_PPLTN_STTS>
              <LIVE_PPLTN_STTS>
                <AREA_NM>광화문·덕수궁</AREA_NM>
                <AREA_CD>POI009</AREA_CD>
                <AREA_CONGEST_LVL>여유</AREA_CONGEST_LVL>
                <AREA_CONGEST_MSG>도보 이동이 자유로워요.</AREA_CONGEST_MSG>
                <AREA_PPLTN_MIN>18000</AREA_PPLTN_MIN>
                <AREA_PPLTN_MAX>20000</AREA_PPLTN_MAX>
                <MALE_PPLTN_RATE>46.1</MALE_PPLTN_RATE>
                <FEMALE_PPLTN_RATE>53.9</FEMALE_PPLTN_RATE>
                <PPLTN_RATE_0>1.2</PPLTN_RATE_0>
                <PPLTN_RATE_10>4.9</PPLTN_RATE_10>
                <PPLTN_RATE_20>23.5</PPLTN_RATE_20>
                <PPLTN_RATE_30>22.4</PPLTN_RATE_30>
                <PPLTN_RATE_40>18.9</PPLTN_RATE_40>
                <PPLTN_RATE_50>15.3</PPLTN_RATE_50>
                <PPLTN_RATE_60>9.1</PPLTN_RATE_60>
                <PPLTN_RATE_70>4.8</PPLTN_RATE_70>
                <RESNT_PPLTN_RATE>18.4</RESNT_PPLTN_RATE>
                <NON_RESNT_PPLTN_RATE>81.6</NON_RESNT_PPLTN_RATE>
                <REPLACE_YN>N</REPLACE_YN>
                <PPLTN_TIME>2026-06-14 18:15</PPLTN_TIME>
              </LIVE_PPLTN_STTS>
            </LIVE_PPLTN_STTS>
          </CITYDATA>
        </SeoulRtd.citydata>
        """;
  }
}
