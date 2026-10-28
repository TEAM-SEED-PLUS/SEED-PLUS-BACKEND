package seed.seedplusbackend.payment.infrastructure.gmpay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
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
import seed.seedplusbackend.payment.application.command.GmpayPaymentFeedbackCommand;
import seed.seedplusbackend.payment.application.result.GmpayPaymentInfo;

@DisplayName("GMPay 결제 통지 검증 연동")
class RestClientGmpayPaymentVerificationAdapterTest {

  private MockRestServiceServer server;
  private RestClientGmpayPaymentVerificationAdapter adapter;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    adapter =
        new RestClientGmpayPaymentVerificationAdapter(
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
  @DisplayName("결제 정보 조회 시 파트너 인증 헤더와 주문번호를 전달한다")
  void getPaymentInfo_requestsPaymentInfo() {
    server
        .expect(once(), requestTo("https://api.gmpay.kr/oapi/p/payment/info?orderNo=gmpay-order-1"))
        .andExpect(method(GET))
        .andExpect(header("devID", "dev-id"))
        .andExpect(header("devToken", "dev-token"))
        .andRespond(
            withSuccess(
                """
                {"success":true,"data":{"exists":true,"orderNo":"gmpay-order-1","state":"COMPLETE","totalPrice":9000,"goodName":"생존율 상세 리포트","memberId":"seed-member","payMethod":"CARD","approvalNo":"approval-no","payDate":"2026-10-28T12:00:00"}}
                """,
                APPLICATION_JSON));

    GmpayPaymentInfo result = adapter.getPaymentInfo("gmpay-order-1");

    assertThat(result.totalPrice()).isEqualTo(9000L);
    assertThat(result.approvalNo()).isEqualTo("approval-no");
    server.verify();
  }

  @Test
  @DisplayName("피드백 토큰이 다르면 결제 통지를 거절한다")
  void validateFeedback_throwsExceptionWhenFeedbackTokenDoesNotMatch() {
    GmpayPaymentFeedbackCommand command =
        new GmpayPaymentFeedbackCommand(
            "seed-member", "invalid-token", "COMPLETE", "gmpay-order-1", null, null, null);

    assertThatThrownBy(() -> adapter.validateFeedback(command))
        .isInstanceOfSatisfying(
            ApplicationException.class,
            exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_GMPAY_FEEDBACK));
  }
}
