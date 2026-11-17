package seed.seedplusbackend.payment.infrastructure.gmpay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.payment.application.port.GmpayPaymentCancellationPort;

@Component
public class RestClientGmpayPaymentCancellationAdapter extends GmpayRestClientSupport
    implements GmpayPaymentCancellationPort {

  public RestClientGmpayPaymentCancellationAdapter(
      @Qualifier("externalRestClientBuilder") RestClient.Builder restClientBuilder,
      GmpayProperties properties) {
    super(restClientBuilder, properties);
  }

  @Override
  public void cancelPayment(String providerOrderNo) {
    validateCredentials();
    try {
      GmpayMemberTokenResponse memberTokenResponse =
          restClient
              .get()
              .uri(uri("/oapi/p/member/token/v2", "memberId", properties.memberId()))
              .header("devID", properties.devId())
              .header("devToken", properties.devToken())
              .retrieve()
              .onStatus(
                  HttpStatusCode::isError,
                  (request, response) ->
                      throwGmpayException(ErrorCode.GMPAY_PAYMENT_CANCEL_FAILED, response))
              .body(GmpayMemberTokenResponse.class);
      String cancelToken = extractCancelToken(memberTokenResponse);

      GmpayPaymentCancellationResponse cancellationResponse =
          restClient
              .post()
              .uri(uri("/oapi/payment/cancel"))
              .body(
                  new GmpayPaymentCancellationRequest(
                      properties.memberId(), providerOrderNo, cancelToken))
              .retrieve()
              .onStatus(
                  HttpStatusCode::isError,
                  (request, response) ->
                      throwGmpayException(ErrorCode.GMPAY_PAYMENT_CANCEL_FAILED, response))
              .body(GmpayPaymentCancellationResponse.class);
      if (cancellationResponse == null || !cancellationResponse.success()) {
        throw new ApplicationException(
            ErrorCode.GMPAY_PAYMENT_CANCEL_FAILED, "invalid response", null);
      }
    } catch (ApplicationException exception) {
      throw exception;
    } catch (RestClientException exception) {
      throw new ApplicationException(ErrorCode.GMPAY_PAYMENT_CANCEL_FAILED, exception);
    }
  }

  private String extractCancelToken(GmpayMemberTokenResponse response) {
    if (response == null
        || !response.success()
        || response.data() == null
        || isBlank(response.data().cancelToken())) {
      throw new ApplicationException(
          ErrorCode.GMPAY_PAYMENT_CANCEL_FAILED, "cancel token is missing", null);
    }
    return response.data().cancelToken();
  }

  private void validateCredentials() {
    validateRequired(
        ErrorCode.GMPAY_PAYMENT_CANCEL_FAILED,
        properties.memberId(),
        properties.devId(),
        properties.devToken());
  }

  private record GmpayMemberTokenResponse(boolean success, GmpayMemberTokenData data) {}

  private record GmpayMemberTokenData(String cancelToken) {}

  private record GmpayPaymentCancellationRequest(
      String memberId, String orderNo, String cancelToken) {}

  private record GmpayPaymentCancellationResponse(boolean success, String message) {}
}
