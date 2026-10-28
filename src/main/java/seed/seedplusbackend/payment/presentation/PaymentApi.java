package seed.seedplusbackend.payment.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.security.AuthenticatedUser;
import seed.seedplusbackend.global.swagger.annotation.ApiErrorCodeExamples;
import seed.seedplusbackend.payment.presentation.dto.CreatePaymentOrderRequest;
import seed.seedplusbackend.payment.presentation.dto.PaymentOrderRequestResponse;

@Tag(name = "결제", description = "결제 API")
public interface PaymentApi {

  @Operation(summary = "GMPay 결제 요청", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiErrorCodeExamples({
    ErrorCode.NOT_FOUND_USER,
    ErrorCode.NOT_FOUND_PAYMENT_PRODUCT,
    ErrorCode.INVALID_PAYMENT_ORDER,
    ErrorCode.GMPAY_PAYMENT_REQUEST_FAILED
  })
  @PostMapping("/orders")
  ResponseEntity<ApiResponse<PaymentOrderRequestResponse>> requestPayment(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Valid @RequestBody CreatePaymentOrderRequest request);
}
