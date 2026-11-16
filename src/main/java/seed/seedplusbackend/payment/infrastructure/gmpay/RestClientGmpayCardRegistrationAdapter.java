package seed.seedplusbackend.payment.infrastructure.gmpay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.payment.application.command.GmpayCardRegistrationRequestCommand;
import seed.seedplusbackend.payment.application.port.GmpayCardRegistrationPort;
import seed.seedplusbackend.payment.application.result.GmpayCardRegistrationRequestResult;

@Component
public class RestClientGmpayCardRegistrationAdapter extends GmpayRestClientSupport
    implements GmpayCardRegistrationPort {

  public RestClientGmpayCardRegistrationAdapter(
      @Qualifier("externalRestClientBuilder") RestClient.Builder restClientBuilder,
      GmpayProperties properties) {
    super(restClientBuilder, properties);
  }

  @Override
  public GmpayCardRegistrationRequestResult requestCardRegistration(
      GmpayCardRegistrationRequestCommand command) {
    validateRequired(
        ErrorCode.GMPAY_CARD_REGISTRATION_REQUEST_FAILED,
        properties.memberId(),
        properties.devId(),
        properties.devToken(),
        properties.feedbackUrl());
    try {
      GmpayCardRegistrationResponse response =
          restClient
              .post()
              .uri(uri("/oapi/p/payment/mp/request"))
              .header("devID", properties.devId())
              .header("devToken", properties.devToken())
              .body(GmpayCardRegistrationRequest.from(properties, command))
              .retrieve()
              .onStatus(
                  HttpStatusCode::isError,
                  (request, response) ->
                      throwGmpayException(ErrorCode.GMPAY_CARD_REGISTRATION_REQUEST_FAILED, response))
              .body(GmpayCardRegistrationResponse.class);
      if (response == null || !response.success() || isBlank(response.mpKey()) || isBlank(response.payUrl())) {
        throw new ApplicationException(
            ErrorCode.GMPAY_CARD_REGISTRATION_REQUEST_FAILED, "invalid response", null);
      }
      return new GmpayCardRegistrationRequestResult(response.mpKey(), response.payUrl());
    } catch (ApplicationException exception) {
      throw exception;
    } catch (RestClientException exception) {
      throw new ApplicationException(ErrorCode.GMPAY_CARD_REGISTRATION_REQUEST_FAILED, exception);
    }
  }

  private record GmpayCardRegistrationRequest(
      String memberId,
      String goodName,
      String customerMobile,
      String customerNo,
      String customerName,
      String customerEmail,
      String feedbackUrl,
      String returnUrl,
      String var1,
      String var2,
      boolean sendSms) {

    private static GmpayCardRegistrationRequest from(
        GmpayProperties properties, GmpayCardRegistrationRequestCommand command) {
      return new GmpayCardRegistrationRequest(
          properties.memberId(),
          command.goodName(),
          command.customerMobile(),
          command.customerNo(),
          command.customerName(),
          command.customerEmail(),
          properties.feedbackUrl(),
          properties.returnUrlOrNull(),
          command.customerNo(),
          command.subscriptionProductCode(),
          false);
    }
  }

  private record GmpayCardRegistrationResponse(boolean success, String mpKey, String payUrl) {}
}
