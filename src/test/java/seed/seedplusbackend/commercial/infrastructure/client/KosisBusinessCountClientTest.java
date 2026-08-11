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
import seed.seedplusbackend.commercial.application.command.KosisBusinessCountCollectCommand;
import seed.seedplusbackend.commercial.application.result.KosisBusinessCountRowResult;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@ExtendWith(OutputCaptureExtension.class)
class KosisBusinessCountClientTest {

  @Test
  void requestException_doesNotRetryClientError() {
    assertThat(KosisBusinessCountClient.requestException(HttpStatus.UNAUTHORIZED).isRetryable())
        .isFalse();
  }

  @Test
  void requestException_retriesServerError() {
    assertThat(
            KosisBusinessCountClient.requestException(HttpStatus.SERVICE_UNAVAILABLE).isRetryable())
        .isTrue();
  }

  @Test
  void fetch_parsesNumericRowsAndSkipsUnpublishedRows() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    KosisBusinessCountClient client = client(builder);
    server
        .expect(queryParam("startPrdDe", "2021"))
        .andExpect(queryParam("endPrdDe", "2023"))
        .andRespond(
            withSuccess(
                """
                [
                  {
                    "ORG_ID": "101",
                    "TBL_ID": "DT_1BD1001",
                    "TBL_NM": "산업별 기업수",
                    "C1": "A",
                    "C1_NM": "전체 산업",
                    "C1_OBJ_NM": "산업별",
                    "ITM_ID": "T01",
                    "ITM_NM": "활동기업",
                    "UNIT_NM": "개",
                    "PRD_SE": "Y",
                    "PRD_DE": "2023",
                    "DT": "1,234,567",
                    "LST_CHN_DE": "2025-12-01"
                  },
                  {
                    "ORG_ID": "101",
                    "TBL_ID": "DT_1BD1001",
                    "TBL_NM": "산업별 기업수",
                    "C1": "A",
                    "C1_NM": "전체 산업",
                    "C1_OBJ_NM": "산업별",
                    "ITM_ID": "T02",
                    "ITM_NM": "소멸기업",
                    "UNIT_NM": "개",
                    "PRD_SE": "Y",
                    "PRD_DE": "2023",
                    "DT": "-",
                    "LST_CHN_DE": "2025-12-01"
                  }
                ]
                """
                    .getBytes(StandardCharsets.UTF_8),
                MediaType.TEXT_HTML));

    List<KosisBusinessCountRowResult> rows =
        client.fetch(new KosisBusinessCountCollectCommand(2021, 2023, null, false));

    assertThat(rows).hasSize(1);
    assertThat(rows.getFirst().industryName()).isEqualTo("전체 산업");
    assertThat(rows.getFirst().itemName()).isEqualTo("활동기업");
    assertThat(rows.getFirst().referenceYear()).isEqualTo(2023);
    assertThat(rows.getFirst().businessCount()).isEqualByComparingTo("1234567");
    server.verify();
  }

  @Test
  void fetch_logsKosisErrorResponse(CapturedOutput output) {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    KosisBusinessCountClient client = client(builder);
    server
        .expect(queryParam("newEstPrdCnt", "3"))
        .andRespond(
            withSuccess(
                """
                {"err":"20","errMsg":"일일 호출 한도를 초과했습니다."}
                """
                    .getBytes(StandardCharsets.UTF_8),
                MediaType.TEXT_HTML));

    assertThatThrownBy(
            () -> client.fetch(new KosisBusinessCountCollectCommand(null, null, 3, false)))
        .isInstanceOfSatisfying(
            ApplicationException.class,
            exception ->
                assertThat(exception.getErrorCode())
                    .isEqualTo(ErrorCode.KOSIS_OPEN_API_INVALID_RESPONSE));

    assertThat(output).contains("err=20", "errMsg=일일 호출 한도를 초과했습니다.");
    server.verify();
  }

  private KosisBusinessCountClient client(RestClient.Builder builder) {
    KosisBusinessCountOpenApiProperties properties =
        new KosisBusinessCountOpenApiProperties(
            "test-key",
            "https://kosis.kr",
            "/openapi/Param/statisticsParameterData.do",
            "101",
            "DT_1BD1001",
            0,
            0,
            0,
            0);
    return new KosisBusinessCountClient(builder, new ObjectMapper(), properties);
  }
}
