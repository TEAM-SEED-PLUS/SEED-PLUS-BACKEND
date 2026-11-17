package seed.seedplusbackend.payment.infrastructure.gmpay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.payment.application.command.GmpayPaymentRequestCommand;
import seed.seedplusbackend.payment.application.result.GmpayPaymentRequestResult;

@DisplayName("GMPay 일반 결제 요청 연동")
class RestClientGmpayPaymentRequestAdapterTest {

  private MockRestServiceServer server;
  private RestClientGmpayPaymentRequestAdapter adapter;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    adapter =
        new RestClientGmpayPaymentRequestAdapter(
            builder,
            new GmpayProperties(
                "https://api.gmpay.kr",
                "seed-member",
                "https://api.seedplus.kr/api/v1/payments/gmpay/feedback",
                "https://seedplus.kr/payments/complete",
                "dev-id",
                "dev-token",
                "feedback-token"));
  }

  @Test
  @DisplayName("주문 정보와 콜백 식별값을 GMPay 결제 요청으로 전달한다")
  void requestPayment_sendsPaymentRequest() {
    server
        .expect(once(), requestTo("https://api.gmpay.kr/oapi/payment/request"))
        .andExpect(method(POST))
        .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
        .andExpect(jsonPath("$.memberId").value("seed-member"))
        .andExpect(jsonPath("$.totalPrice").value(9000))
        .andExpect(
            jsonPath("$.feedbackUrl")
                .value("https://api.seedplus.kr/api/v1/payments/gmpay/feedback"))
        .andExpect(jsonPath("$.var1").value("order-key"))
        .andExpect(jsonPath("$.var2").value("SURVIVAL_REPORT"))
        .andRespond(
            withSuccess(
                """
                {"success":true,"message":"OK","orderNo":"gmpay-order-1","payUrl":"https://pay.gmpay.kr/1"}
                """,
                APPLICATION_JSON));

    GmpayPaymentRequestResult result = adapter.requestPayment(command());

    assertThat(result.providerOrderNo()).isEqualTo("gmpay-order-1");
    assertThat(result.payUrl()).isEqualTo("https://pay.gmpay.kr/1");
    server.verify();
  }

  @Test
  @DisplayName("GMPay가 실패 응답을 반환하면 결제 요청 예외를 발생시킨다")
  void requestPayment_throwsExceptionWhenGmpayReturnsFailure() {
    server
        .expect(requestTo("https://api.gmpay.kr/oapi/payment/request"))
        .andRespond(withSuccess("{\"success\":false,\"message\":\"failed\"}", APPLICATION_JSON));

    assertThatThrownBy(() -> adapter.requestPayment(command()))
        .isInstanceOfSatisfying(
            ApplicationException.class,
            exception ->
                assertThat(exception.getErrorCode())
                    .isEqualTo(ErrorCode.GMPAY_PAYMENT_REQUEST_FAILED));
    server.verify();
  }

  private GmpayPaymentRequestCommand command() {
    return new GmpayPaymentRequestCommand(
        "생존율 상세 리포트", 9000, "01012345678", "user@example.com", "order-key", "SURVIVAL_REPORT");
  }
}
