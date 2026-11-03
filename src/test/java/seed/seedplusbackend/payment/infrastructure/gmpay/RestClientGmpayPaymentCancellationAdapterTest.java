package seed.seedplusbackend.payment.infrastructure.gmpay;

import static org.springframework.http.HttpMethod.GET;
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

@DisplayName("GMPay 결제 취소 연동")
class RestClientGmpayPaymentCancellationAdapterTest {

  private MockRestServiceServer server;
  private RestClientGmpayPaymentCancellationAdapter adapter;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    adapter =
        new RestClientGmpayPaymentCancellationAdapter(
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
  @DisplayName("취소 토큰을 조회한 뒤 주문번호로 결제를 취소한다")
  void cancelPayment_requestsMemberTokenAndCancelsPayment() {
    server
        .expect(once(), requestTo("https://api.gmpay.kr/oapi/p/member/token/v2?memberId=seed-member"))
        .andExpect(method(GET))
        .andExpect(header("devID", "dev-id"))
        .andExpect(header("devToken", "dev-token"))
        .andRespond(
            withSuccess(
                """
                {"success":true,"data":{"cancelToken":"cancel-token","feedbackToken":"feedback-token"}}
                """,
                APPLICATION_JSON));
    server
        .expect(once(), requestTo("https://api.gmpay.kr/oapi/payment/cancel"))
        .andExpect(method(POST))
        .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
        .andExpect(jsonPath("$.memberId").value("seed-member"))
        .andExpect(jsonPath("$.orderNo").value("gmpay-order-1"))
        .andExpect(jsonPath("$.cancelToken").value("cancel-token"))
        .andRespond(withSuccess("{\"success\":true,\"message\":\"OK\"}", APPLICATION_JSON));

    adapter.cancelPayment("gmpay-order-1");

    server.verify();
  }
}
