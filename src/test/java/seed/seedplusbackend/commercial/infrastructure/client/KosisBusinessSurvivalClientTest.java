package seed.seedplusbackend.commercial.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import seed.seedplusbackend.commercial.application.command.KosisBusinessSurvivalCollectCommand;
import seed.seedplusbackend.commercial.application.result.KosisBusinessSurvivalRowResult;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@ExtendWith(OutputCaptureExtension.class)
class KosisBusinessSurvivalClientTest {

  @Test
  void requestException_doesNotRetryClientError() {
    assertThat(KosisBusinessSurvivalClient.requestException(HttpStatus.UNAUTHORIZED).isRetryable())
        .isFalse();
  }

  @Test
  void requestException_retriesServerError() {
    assertThat(
            KosisBusinessSurvivalClient.requestException(HttpStatus.SERVICE_UNAVAILABLE)
                .isRetryable())
        .isTrue();
  }

  @Test
  void fetch_parsesJsonBodyEvenWhenKosisRespondsWithTextHtmlContentType() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    KosisBusinessSurvivalOpenApiProperties properties =
        new KosisBusinessSurvivalOpenApiProperties(
            "test-key",
            "https://kosis.kr",
            "/openapi/Param/statisticsParameterData.do",
            "101",
            "DT_2BD1003",
            0,
            0,
            0,
            0);
    KosisBusinessSurvivalClient client =
        new KosisBusinessSurvivalClient(builder, new ObjectMapper(), properties);
    server
        .expect(queryParam("startPrdDe", "2021"))
        .andExpect(queryParam("endPrdDe", "2022"))
        .andRespond(
            withSuccess(
                """
                [
                  {
                    "ORG_ID": "101",
                    "TBL_ID": "DT_2BD1003",
                    "TBL_NM": "산업별 신생기업 생존율",
                    "C1": "0",
                    "C1_NM": "전체",
                    "C1_OBJ_NM": "산업별",
                    "ITM_ID": "T001",
                    "ITM_NM": "1년 생존기업 수",
                    "UNIT_NM": "%",
                    "PRD_SE": "A",
                    "PRD_DE": "2022",
                    "DT": "670768",
                    "LST_CHN_DE": "2024-12-26"
                  },
                  {
                    "ORG_ID": "101",
                    "TBL_ID": "DT_2BD1003",
                    "TBL_NM": "산업별 신생기업 생존율",
                    "C1": "0",
                    "C1_NM": "전체",
                    "C1_OBJ_NM": "산업별",
                    "ITM_ID": "T01",
                    "ITM_NM": "1년 생존율",
                    "UNIT_NM": "%",
                    "PRD_SE": "A",
                    "PRD_DE": "2022",
                    "DT": "64.1",
                    "LST_CHN_DE": "2024-12-26"
                  },
                  {
                    "ORG_ID": "101",
                    "TBL_ID": "DT_2BD1003",
                    "TBL_NM": "산업별 신생기업 생존율",
                    "C1": "0",
                    "C1_NM": "전체",
                    "C1_OBJ_NM": "산업별",
                    "ITM_ID": "T02",
                    "ITM_NM": "2년 생존율",
                    "UNIT_NM": "%",
                    "PRD_SE": "A",
                    "PRD_DE": "2022",
                    "DT": "-",
                    "LST_CHN_DE": "2024-12-26"
                  }
                ]
                """
                    .getBytes(StandardCharsets.UTF_8),
                MediaType.TEXT_HTML));

    List<KosisBusinessSurvivalRowResult> rows =
        client.fetch(new KosisBusinessSurvivalCollectCommand(2021, 2022, null, false));

    assertThat(rows).hasSize(1);
    assertThat(rows.getFirst().industryName()).isEqualTo("전체");
    assertThat(rows.getFirst().itemName()).isEqualTo("1년 생존율");
    assertThat(rows.getFirst().referenceYear()).isEqualTo(2022);
    assertThat(rows.getFirst().survivalRate()).isEqualByComparingTo("64.1");
    server.verify();
  }

  @Test
  void fetch_logsKosisErrorResponse(CapturedOutput output) {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    KosisBusinessSurvivalOpenApiProperties properties =
        new KosisBusinessSurvivalOpenApiProperties(
            "test-key",
            "https://kosis.kr",
            "/openapi/Param/statisticsParameterData.do",
            "101",
            "DT_2BD1003",
            0,
            0,
            0,
            0);
    KosisBusinessSurvivalClient client =
        new KosisBusinessSurvivalClient(builder, new ObjectMapper(), properties);
    server
        .expect(queryParam("startPrdDe", "2021"))
        .andExpect(queryParam("endPrdDe", "2022"))
        .andRespond(
            withSuccess(
                """
                {"err":"20","errMsg":"일일 호출 한도를 초과했습니다."}
                """
                    .getBytes(StandardCharsets.UTF_8),
                MediaType.TEXT_HTML));

    assertThatThrownBy(
            () -> client.fetch(new KosisBusinessSurvivalCollectCommand(2021, 2022, null, false)))
        .isInstanceOfSatisfying(
            ApplicationException.class,
            exception ->
                assertThat(exception.getErrorCode())
                    .isEqualTo(ErrorCode.KOSIS_OPEN_API_INVALID_RESPONSE));

    assertThat(output).contains("err=20", "errMsg=일일 호출 한도를 초과했습니다.");
    server.verify();
  }
}
