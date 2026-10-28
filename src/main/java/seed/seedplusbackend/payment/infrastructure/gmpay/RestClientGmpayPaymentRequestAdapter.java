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
import seed.seedplusbackend.payment.application.command.GmpayPaymentRequestCommand;
import seed.seedplusbackend.payment.application.port.GmpayPaymentRequestPort;
import seed.seedplusbackend.payment.application.result.GmpayPaymentRequestResult;

@Component
public class RestClientGmpayPaymentRequestAdapter implements GmpayPaymentRequestPort {

  private final GmpayProperties properties;
  private final RestClient restClient;

  public RestClientGmpayPaymentRequestAdapter(
      @Qualifier("externalRestClientBuilder") RestClient.Builder restClientBuilder,
      GmpayProperties properties) {
    this.restClient = restClientBuilder.build();
    this.properties = properties;
  }

  @Override
  public GmpayPaymentRequestResult requestPayment(GmpayPaymentRequestCommand command) {
    if (properties.memberId() == null || properties.memberId().isBlank()) {
      throw new ApplicationException(ErrorCode.GMPAY_PAYMENT_REQUEST_FAILED, "memberId is missing", null);
    }
    if (properties.feedbackUrl() == null || properties.feedbackUrl().isBlank()) {
      throw new ApplicationException(ErrorCode.GMPAY_PAYMENT_REQUEST_FAILED, "feedbackUrl is missing", null);
    }

    try {
      GmpayPaymentRequestResponse response =
          restClient
              .post()
              .uri(paymentRequestUri())
              .body(GmpayPaymentRequest.from(properties, command))
              .retrieve()
              .onStatus(HttpStatusCode::isError, this::throwGmpayPaymentRequestException)
              .body(GmpayPaymentRequestResponse.class);
      if (response == null || !response.success() || response.orderNo() == null || response.payUrl() == null) {
        throw new ApplicationException(ErrorCode.GMPAY_PAYMENT_REQUEST_FAILED, "invalid response", null);
      }
      return new GmpayPaymentRequestResult(response.orderNo(), response.payUrl());
    } catch (ApplicationException exception) {
      throw exception;
    } catch (RestClientException exception) {
      throw new ApplicationException(ErrorCode.GMPAY_PAYMENT_REQUEST_FAILED, exception);
    }
  }

  private URI paymentRequestUri() {
    String baseUrl = properties.baseUrl().replaceAll("/+$", "");
    return UriComponentsBuilder.fromUriString(baseUrl + "/oapi/payment/request").build().toUri();
  }

  private void throwGmpayPaymentRequestException(
      org.springframework.http.HttpRequest request,
      org.springframework.http.client.ClientHttpResponse response) {
    try {
      throw new ApplicationException(
          ErrorCode.GMPAY_PAYMENT_REQUEST_FAILED,
          "status=%d".formatted(response.getStatusCode().value()),
          null);
    } catch (java.io.IOException exception) {
      throw new ApplicationException(ErrorCode.GMPAY_PAYMENT_REQUEST_FAILED, exception);
    }
  }

  private record GmpayPaymentRequest(
      String memberId,
      long totalPrice,
      String goodName,
      String mobile,
      String email,
      String feedbackUrl,
      String returnUrl,
      String var1,
      String var2) {

    private static GmpayPaymentRequest from(
        GmpayProperties properties, GmpayPaymentRequestCommand command) {
      return new GmpayPaymentRequest(
          properties.memberId(),
          command.totalPrice(),
          command.goodName(),
          command.buyerMobile(),
          command.buyerEmail(),
          properties.feedbackUrl(),
          blankToNull(properties.returnUrl()),
          command.orderKey(),
          command.productCode());
    }

    private static String blankToNull(String value) {
      return value == null || value.isBlank() ? null : value;
    }
  }

  private record GmpayPaymentRequestResponse(
      boolean success, String message, String orderNo, String payUrl) {}
}
