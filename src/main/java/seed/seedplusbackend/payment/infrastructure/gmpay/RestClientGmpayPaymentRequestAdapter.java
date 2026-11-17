package seed.seedplusbackend.payment.infrastructure.gmpay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.payment.application.command.GmpayPaymentRequestCommand;
import seed.seedplusbackend.payment.application.port.GmpayPaymentRequestPort;
import seed.seedplusbackend.payment.application.result.GmpayPaymentRequestResult;

@Component
public class RestClientGmpayPaymentRequestAdapter extends GmpayRestClientSupport
    implements GmpayPaymentRequestPort {

  public RestClientGmpayPaymentRequestAdapter(
      @Qualifier("externalRestClientBuilder") RestClient.Builder restClientBuilder,
      GmpayProperties properties) {
    super(restClientBuilder, properties);
  }

  @Override
  public GmpayPaymentRequestResult requestPayment(GmpayPaymentRequestCommand command) {
    validateRequired(
        ErrorCode.GMPAY_PAYMENT_REQUEST_FAILED, properties.memberId(), properties.feedbackUrl());

    try {
      GmpayPaymentRequestResponse response =
          restClient
              .post()
              .uri(uri("/oapi/payment/request"))
              .body(GmpayPaymentRequest.from(properties, command))
              .retrieve()
              .onStatus(
                  HttpStatusCode::isError,
                  (request, clientResponse) ->
                      throwGmpayException(ErrorCode.GMPAY_PAYMENT_REQUEST_FAILED, clientResponse))
              .body(GmpayPaymentRequestResponse.class);
      if (response == null
          || !response.success()
          || response.orderNo() == null
          || response.payUrl() == null) {
        throw new ApplicationException(
            ErrorCode.GMPAY_PAYMENT_REQUEST_FAILED, "invalid response", null);
      }
      return new GmpayPaymentRequestResult(response.orderNo(), response.payUrl());
    } catch (ApplicationException exception) {
      throw exception;
    } catch (RestClientException exception) {
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
          properties.returnUrlOrNull(),
          command.orderKey(),
          command.productCode());
    }
  }

  private record GmpayPaymentRequestResponse(
      boolean success, String message, String orderNo, String payUrl) {}
}
