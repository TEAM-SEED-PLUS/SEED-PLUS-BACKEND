package seed.seedplusbackend.payment.infrastructure.gmpay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import seed.seedplusbackend.payment.application.command.GmpayCardRegistrationRequestCommand;
import seed.seedplusbackend.payment.application.result.GmpayCardRegistrationRequestResult;

@DisplayName("GMPay 카드 등록 요청 연동")
class RestClientGmpayCardRegistrationAdapterTest {

  private MockRestServiceServer server;
  private RestClientGmpayCardRegistrationAdapter adapter;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    adapter =
        new RestClientGmpayCardRegistrationAdapter(
            builder,
            new GmpayProperties(
                "https://api.gmpay.kr",
                "seed-member",
                "https://api.seedplus.kr/api/v1/payments/gmpay/feedback",
                null,
                "dev-id",
                "dev-token",
                "feedback-token"));
  }

  @Test
  @DisplayName("파트너 인증정보와 구매자 정보를 GMPay 카드 등록 요청으로 전달한다")
  void requestCardRegistration_sendsCardRegistrationRequest() {
    server
        .expect(once(), requestTo("https://api.gmpay.kr/oapi/p/payment/mp/request"))
        .andExpect(method(POST))
        .andExpect(header("devID", "dev-id"))
        .andExpect(header("devToken", "dev-token"))
        .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
        .andExpect(jsonPath("$.memberId").value("seed-member"))
        .andExpect(jsonPath("$.customerNo").value("1"))
        .andExpect(jsonPath("$.var2").value("PRO_MONTHLY"))
        .andRespond(
            withSuccess(
                """
                {"success":true,"mpKey":"mp-key","payUrl":"https://pay.gmpay.kr/card"}
                """,
                APPLICATION_JSON));

    GmpayCardRegistrationRequestResult result =
        adapter.requestCardRegistration(
            new GmpayCardRegistrationRequestCommand(
                "PRO 월간 구독",
                "01012345678",
                "1",
                "사용자",
                "user@example.com",
                "PRO_MONTHLY"));

    assertThat(result.mpKey()).isEqualTo("mp-key");
    assertThat(result.payUrl()).isEqualTo("https://pay.gmpay.kr/card");
    server.verify();
  }
}
