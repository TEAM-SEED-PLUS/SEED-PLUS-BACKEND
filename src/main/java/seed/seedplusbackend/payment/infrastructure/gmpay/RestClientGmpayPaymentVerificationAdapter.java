package seed.seedplusbackend.payment.infrastructure.gmpay;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.payment.application.command.GmpayPaymentFeedbackCommand;
import seed.seedplusbackend.payment.application.port.GmpayPaymentVerificationPort;
import seed.seedplusbackend.payment.application.result.GmpayPaymentInfo;

@Component
public class RestClientGmpayPaymentVerificationAdapter extends GmpayRestClientSupport
    implements GmpayPaymentVerificationPort {

  public RestClientGmpayPaymentVerificationAdapter(
      @Qualifier("externalRestClientBuilder") RestClient.Builder restClientBuilder,
      GmpayProperties properties) {
    super(restClientBuilder, properties);
  }

  @Override
  public void validateFeedback(GmpayPaymentFeedbackCommand command) {
    if (isBlank(properties.memberId())
        || isBlank(properties.feedbackToken())
        || !properties.memberId().equals(command.memberId())
        || !MessageDigest.isEqual(
            properties.feedbackToken().getBytes(StandardCharsets.UTF_8),
            command.feedbackToken() == null
                ? new byte[0]
                : command.feedbackToken().getBytes(StandardCharsets.UTF_8))) {
      throw new ApplicationException(ErrorCode.INVALID_GMPAY_FEEDBACK);
    }
  }

  @Override
  public GmpayPaymentInfo getPaymentInfo(String providerOrderNo) {
    validatePaymentInfoCredentials();
    try {
      GmpayPaymentInfoResponse response =
          restClient
              .get()
              .uri(uri("/oapi/p/payment/info", "orderNo", providerOrderNo))
              .header("devID", properties.devId())
              .header("devToken", properties.devToken())
              .retrieve()
              .onStatus(
                  HttpStatusCode::isError,
                  (request, response) ->
                      throwGmpayException(ErrorCode.GMPAY_PAYMENT_INFO_REQUEST_FAILED, response))
              .body(GmpayPaymentInfoResponse.class);
      if (response == null || !response.success() || response.data() == null || !response.data().exists()) {
        throw new ApplicationException(ErrorCode.GMPAY_PAYMENT_INFO_REQUEST_FAILED, "invalid response", null);
      }
      if (!properties.memberId().equals(response.data().memberId())) {
        throw new ApplicationException(ErrorCode.GMPAY_PAYMENT_VERIFICATION_FAILED);
      }
      return response.data().toResult();
    } catch (ApplicationException exception) {
      throw exception;
    } catch (RestClientException exception) {
      throw new ApplicationException(ErrorCode.GMPAY_PAYMENT_INFO_REQUEST_FAILED, exception);
    }
  }

  private void validatePaymentInfoCredentials() {
    validateRequired(
        ErrorCode.GMPAY_PAYMENT_INFO_REQUEST_FAILED, properties.devId(), properties.devToken());
  }

  private record GmpayPaymentInfoResponse(boolean success, GmpayPaymentInfoData data) {}

  private record GmpayPaymentInfoData(
      boolean exists,
      String orderNo,
      String state,
      Long totalPrice,
      String goodName,
      String memberId,
      String payMethod,
      String approvalNo,
      LocalDateTime payDate) {

    private GmpayPaymentInfo toResult() {
      if (totalPrice == null) {
        throw new ApplicationException(ErrorCode.GMPAY_PAYMENT_INFO_REQUEST_FAILED, "totalPrice is missing", null);
      }
      return new GmpayPaymentInfo(
          orderNo, state, totalPrice, goodName, memberId, payMethod, approvalNo, payDate);
    }
  }
}
