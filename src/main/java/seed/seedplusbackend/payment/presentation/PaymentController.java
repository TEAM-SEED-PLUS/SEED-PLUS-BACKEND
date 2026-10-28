package seed.seedplusbackend.payment.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.security.AuthenticatedUser;
import seed.seedplusbackend.payment.application.PaymentOrderService;
import seed.seedplusbackend.payment.presentation.dto.CreatePaymentOrderRequest;
import seed.seedplusbackend.payment.presentation.dto.PaymentOrderRequestResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController implements PaymentApi {

  private final PaymentOrderService paymentOrderService;

  @Override
  public ResponseEntity<ApiResponse<PaymentOrderRequestResponse>> requestPayment(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Valid @RequestBody CreatePaymentOrderRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            PaymentOrderRequestResponse.from(
                paymentOrderService.requestPayment(authenticatedUser.getId(), request.toCommand()))));
  }
}
