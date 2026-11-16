package seed.seedplusbackend.payment.infrastructure.gmpay;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.payment.application.command.GmpayRecurringPaymentCommand;
import seed.seedplusbackend.payment.application.port.GmpayRecurringPaymentPort;
import seed.seedplusbackend.payment.application.result.GmpayRecurringPaymentResult;

@Component
public class RestClientGmpayRecurringPaymentAdapter extends GmpayRestClientSupport
    implements GmpayRecurringPaymentPort {

  public RestClientGmpayRecurringPaymentAdapter(
      @Qualifier("externalRestClientBuilder") RestClient.Builder restClientBuilder,
      GmpayProperties properties) {
    super(restClientBuilder, properties);
  }

  @Override
  public GmpayRecurringPaymentResult requestPayment(GmpayRecurringPaymentCommand command) {
    validateRequired(
        ErrorCode.GMPAY_RECURRING_PAYMENT_FAILED,
        properties.memberId(),
        properties.devId(),
        properties.devToken(),
        properties.feedbackUrl());
    try {
      GmpayRecurringPaymentResponse response =
          restClient
              .post()
              .uri(uri("/oapi/p/payment/mp/payment"))
              .header("devID", properties.devId())
              .header("devToken", properties.devToken())
              .body(GmpayRecurringPaymentRequest.from(properties, command))
              .retrieve()
              .onStatus(
                  HttpStatusCode::isError,
                  (request, response) ->
                      throwGmpayException(ErrorCode.GMPAY_RECURRING_PAYMENT_FAILED, response))
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
      if (amt == null || orderNo == null || orderNo.isBlank()) {
        throw new ApplicationException(ErrorCode.GMPAY_RECURRING_PAYMENT_FAILED, "invalid payInfo", null);
      }
      return new GmpayRecurringPaymentResult(
          orderNo, amt, "CARD", authCode, cstUrl, parsePaidAt(authDate));
    }

    private static LocalDateTime parsePaidAt(String authDate) {
      if (authDate == null || authDate.isBlank()) {
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
