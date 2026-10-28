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
import seed.seedplusbackend.payment.application.command.GmpayCardRegistrationRequestCommand;
import seed.seedplusbackend.payment.application.port.GmpayCardRegistrationPort;
import seed.seedplusbackend.payment.application.result.GmpayCardRegistrationRequestResult;

@Component
public class RestClientGmpayCardRegistrationAdapter implements GmpayCardRegistrationPort {

  private final GmpayProperties properties;
  private final RestClient restClient;

  public RestClientGmpayCardRegistrationAdapter(
      @Qualifier("externalRestClientBuilder") RestClient.Builder restClientBuilder,
      GmpayProperties properties) {
    this.properties = properties;
    this.restClient = restClientBuilder.build();
  }

  @Override
  public GmpayCardRegistrationRequestResult requestCardRegistration(
      GmpayCardRegistrationRequestCommand command) {
    validateProperties();
    try {
      GmpayCardRegistrationResponse response =
          restClient
              .post()
              .uri(cardRegistrationUri())
              .header("devID", properties.devId())
              .header("devToken", properties.devToken())
              .body(GmpayCardRegistrationRequest.from(properties, command))
              .retrieve()
              .onStatus(HttpStatusCode::isError, this::throwGmpayCardRegistrationException)
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

  private URI cardRegistrationUri() {
    String baseUrl = properties.baseUrl().replaceAll("/+$", "");
    return UriComponentsBuilder.fromUriString(baseUrl + "/oapi/p/payment/mp/request").build().toUri();
  }

  private void validateProperties() {
    if (isBlank(properties.memberId())
        || isBlank(properties.devId())
        || isBlank(properties.devToken())
        || isBlank(properties.feedbackUrl())) {
      throw new ApplicationException(
          ErrorCode.GMPAY_CARD_REGISTRATION_REQUEST_FAILED, "required properties are missing", null);
    }
  }

  private void throwGmpayCardRegistrationException(
      org.springframework.http.HttpRequest request,
      org.springframework.http.client.ClientHttpResponse response) {
    try {
      throw new ApplicationException(
          ErrorCode.GMPAY_CARD_REGISTRATION_REQUEST_FAILED,
          "status=%d".formatted(response.getStatusCode().value()),
          null);
    } catch (java.io.IOException exception) {
      throw new ApplicationException(ErrorCode.GMPAY_CARD_REGISTRATION_REQUEST_FAILED, exception);
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
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
          blankToNull(properties.returnUrl()),
          command.customerNo(),
          command.subscriptionProductCode(),
          false);
    }

    private static String blankToNull(String value) {
      return value == null || value.isBlank() ? null : value;
    }
  }

  private record GmpayCardRegistrationResponse(boolean success, String mpKey, String payUrl) {}
}
