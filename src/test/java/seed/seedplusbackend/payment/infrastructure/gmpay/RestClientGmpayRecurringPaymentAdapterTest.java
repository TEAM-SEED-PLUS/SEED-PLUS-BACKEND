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
import seed.seedplusbackend.payment.application.command.GmpayRecurringPaymentCommand;
import seed.seedplusbackend.payment.application.result.GmpayRecurringPaymentResult;

@DisplayName("GMPay 등록결제 연동")
class RestClientGmpayRecurringPaymentAdapterTest {

  private MockRestServiceServer server;
  private RestClientGmpayRecurringPaymentAdapter adapter;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    adapter =
        new RestClientGmpayRecurringPaymentAdapter(
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
  @DisplayName("암호화 키를 제외한 결제 정보만 GMPay 등록결제 요청으로 전달한다")
  void requestPayment_sendsRecurringPaymentRequest() {
    server
        .expect(once(), requestTo("https://api.gmpay.kr/oapi/p/payment/mp/payment"))
        .andExpect(method(POST))
        .andExpect(header("devID", "dev-id"))
        .andExpect(header("devToken", "dev-token"))
        .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
        .andExpect(jsonPath("$.mpKey").value("mp-key"))
        .andExpect(jsonPath("$.totalPrice").value(5900))
        .andExpect(jsonPath("$.var2").value("PRO_MONTHLY"))
        .andRespond(
            withSuccess(
                """
                {"success":true,"payInfo":{"amt":5900,"orderNo":"gmpay-order-1","authCode":"approval-no","authDate":"2026-11-03T12:00:00","appCardName":"카드사","cstUrl":"https://pay.gmpay.kr/receipt"}}
                """,
                APPLICATION_JSON));

    GmpayRecurringPaymentResult result =
        adapter.requestPayment(
            new GmpayRecurringPaymentCommand(
                "mp-key",
                5900L,
                "PRO 월간 구독",
                "01012345678",
                "user@example.com",
                "사용자",
                "order-key",
                "PRO_MONTHLY"));

    assertThat(result.providerOrderNo()).isEqualTo("gmpay-order-1");
    assertThat(result.amount()).isEqualTo(5900L);
    server.verify();
  }
}
