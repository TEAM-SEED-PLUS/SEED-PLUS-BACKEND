package seed.seedplusbackend.payment.infrastructure.gmpay;

import java.net.URI;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.payment.application.command.GmpayRecurringPaymentCommand;
import seed.seedplusbackend.payment.application.port.GmpayRecurringPaymentPort;
import seed.seedplusbackend.payment.application.result.GmpayRecurringPaymentResult;

@Component
public class RestClientGmpayRecurringPaymentAdapter implements GmpayRecurringPaymentPort {

  private final GmpayProperties properties;
  private final RestClient restClient;

  public RestClientGmpayRecurringPaymentAdapter(
      @Qualifier("externalRestClientBuilder") RestClient.Builder restClientBuilder,
      GmpayProperties properties) {
    this.properties = properties;
    this.restClient = restClientBuilder.build();
  }

  @Override
  public GmpayRecurringPaymentResult requestPayment(GmpayRecurringPaymentCommand command) {
    validateProperties();
    try {
      GmpayRecurringPaymentResponse response =
          restClient
              .post()
              .uri(recurringPaymentUri())
              .header("devID", properties.devId())
              .header("devToken", properties.devToken())
              .body(GmpayRecurringPaymentRequest.from(properties, command))
              .retrieve()
              .onStatus(HttpStatusCode::isError, this::throwGmpayRecurringPaymentException)
              .body(GmpayRecurringPaymentResponse.class);
      if (response == null || !response.success() || response.payInfo() == null) {
        throw new ApplicationException(ErrorCode.GMPAY_RECURRING_PAYMENT_FAILED, "invalid response", null);
      }
      return response.payInfo().toResult();
    } catch (ApplicationException exception) {
      throw exception;
    } catch (RestClientException exception) {
      throw new ApplicationException(ErrorCode.GMPAY_RECURRING_PAYMENT_FAILED, exception);
    }
  }

  private URI recurringPaymentUri() {
    String baseUrl = properties.baseUrl().replaceAll("/+$", "");
    return UriComponentsBuilder.fromUriString(baseUrl + "/oapi/p/payment/mp/payment").build().toUri();
  }

  private void validateProperties() {
    if (isBlank(properties.memberId())
        || isBlank(properties.devId())
        || isBlank(properties.devToken())
        || isBlank(properties.feedbackUrl())) {
      throw new ApplicationException(
          ErrorCode.GMPAY_RECURRING_PAYMENT_FAILED, "required properties are missing", null);
    }
  }

  private void throwGmpayRecurringPaymentException(
      org.springframework.http.HttpRequest request,
      org.springframework.http.client.ClientHttpResponse response) {
    try {
      throw new ApplicationException(
          ErrorCode.GMPAY_RECURRING_PAYMENT_FAILED,
          "status=%d".formatted(response.getStatusCode().value()),
          null);
    } catch (java.io.IOException exception) {
      throw new ApplicationException(ErrorCode.GMPAY_RECURRING_PAYMENT_FAILED, exception);
    }
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private record GmpayRecurringPaymentRequest(
      String memberId,
      String mpKey,
      long totalPrice,
      String goodName,
      String buyMobile,
      String buyEmail,
      String buyName,
      String feedbackUrl,
      String var1,
      String var2) {

    private static GmpayRecurringPaymentRequest from(
        GmpayProperties properties, GmpayRecurringPaymentCommand command) {
      return new GmpayRecurringPaymentRequest(
          properties.memberId(),
          command.mpKey(),
          command.totalPrice(),
          command.goodName(),
          command.buyerMobile(),
          command.buyerEmail(),
          command.buyerName(),
          properties.feedbackUrl(),
          command.orderKey(),
          command.productCode());
    }
  }

  private record GmpayRecurringPaymentResponse(boolean success, GmpayPayInfo payInfo) {}

  private record GmpayPayInfo(
      Long amt,
      String orderNo,
      String authCode,
      String authDate,
      String appCardName,
      String cstUrl) {

    private GmpayRecurringPaymentResult toResult() {
      if (amt == null || isBlank(orderNo)) {
        throw new ApplicationException(ErrorCode.GMPAY_RECURRING_PAYMENT_FAILED, "invalid payInfo", null);
      }
      return new GmpayRecurringPaymentResult(
          orderNo, amt, "CARD", authCode, cstUrl, parsePaidAt(authDate));
    }

    private static LocalDateTime parsePaidAt(String authDate) {
      if (isBlank(authDate)) {
        return null;
      }
      try {
        return LocalDateTime.parse(authDate);
      } catch (java.time.format.DateTimeParseException exception) {
        try {
          return LocalDateTime.parse(
              authDate, java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        } catch (java.time.format.DateTimeParseException ignored) {
          return null;
        }
      }
    }
  }
}
