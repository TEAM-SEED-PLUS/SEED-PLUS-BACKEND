package seed.seedplusbackend.payment.infrastructure.gmpay;

import java.net.URI;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.payment.application.port.GmpayPaymentCancellationPort;

@Component
public class RestClientGmpayPaymentCancellationAdapter implements GmpayPaymentCancellationPort {

  private final GmpayProperties properties;
  private final RestClient restClient;

  public RestClientGmpayPaymentCancellationAdapter(
      @Qualifier("externalRestClientBuilder") RestClient.Builder restClientBuilder,
      GmpayProperties properties) {
    this.properties = properties;
    this.restClient = restClientBuilder.build();
  }

  @Override
  public void cancelPayment(String providerOrderNo) {
    validateCredentials();
    try {
      GmpayMemberTokenResponse memberTokenResponse =
          restClient
              .get()
              .uri(memberTokenUri())
              .header("devID", properties.devId())
              .header("devToken", properties.devToken())
              .retrieve()
              .onStatus(HttpStatusCode::isError, this::throwCancellationException)
              .body(GmpayMemberTokenResponse.class);
      String cancelToken = extractCancelToken(memberTokenResponse);

      GmpayPaymentCancellationResponse cancellationResponse =
          restClient
              .post()
              .uri(paymentCancellationUri())
              .body(new GmpayPaymentCancellationRequest(properties.memberId(), providerOrderNo, cancelToken))
              .retrieve()
              .onStatus(HttpStatusCode::isError, this::throwCancellationException)
              .body(GmpayPaymentCancellationResponse.class);
      if (cancellationResponse == null || !cancellationResponse.success()) {
        throw new ApplicationException(ErrorCode.GMPAY_PAYMENT_CANCEL_FAILED, "invalid response", null);
      }
    } catch (ApplicationException exception) {
      throw exception;
    } catch (RestClientException exception) {
      throw new ApplicationException(ErrorCode.GMPAY_PAYMENT_CANCEL_FAILED, exception);
    }
  }

  private URI memberTokenUri() {
    String baseUrl = properties.baseUrl().replaceAll("/+$", "");
    return UriComponentsBuilder.fromUriString(baseUrl + "/oapi/p/member/token/v2")
        .queryParam("memberId", properties.memberId())
        .build()
        .toUri();
  }

  private URI paymentCancellationUri() {
    String baseUrl = properties.baseUrl().replaceAll("/+$", "");
    return UriComponentsBuilder.fromUriString(baseUrl + "/oapi/payment/cancel").build().toUri();
  }

  private String extractCancelToken(GmpayMemberTokenResponse response) {
    if (response == null
        || !response.success()
        || response.data() == null
        || isBlank(response.data().cancelToken())) {
      throw new ApplicationException(ErrorCode.GMPAY_PAYMENT_CANCEL_FAILED, "cancel token is missing", null);
    }
    return response.data().cancelToken();
  }

  private void validateCredentials() {
    if (isBlank(properties.memberId())
        || isBlank(properties.devId())
        || isBlank(properties.devToken())) {
      throw new ApplicationException(
          ErrorCode.GMPAY_PAYMENT_CANCEL_FAILED, "cancellation credentials are missing", null);
    }
  }

  private void throwCancellationException(
      org.springframework.http.HttpRequest request,
      org.springframework.http.client.ClientHttpResponse response) {
    try {
      throw new ApplicationException(
          ErrorCode.GMPAY_PAYMENT_CANCEL_FAILED,
          "status=%d".formatted(response.getStatusCode().value()),
          null);
    } catch (java.io.IOException exception) {
      throw new ApplicationException(ErrorCode.GMPAY_PAYMENT_CANCEL_FAILED, exception);
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private record GmpayMemberTokenResponse(boolean success, GmpayMemberTokenData data) {}

  private record GmpayMemberTokenData(String cancelToken) {}

  private record GmpayPaymentCancellationRequest(
      String memberId, String orderNo, String cancelToken) {}

  private record GmpayPaymentCancellationResponse(boolean success, String message) {}
}
