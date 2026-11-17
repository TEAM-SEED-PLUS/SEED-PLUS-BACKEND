package seed.seedplusbackend.payment.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.security.AuthenticatedUser;
import seed.seedplusbackend.global.swagger.annotation.ApiErrorCodeExamples;
import seed.seedplusbackend.payment.presentation.dto.CardRegistrationRequest;
import seed.seedplusbackend.payment.presentation.dto.CardRegistrationResponse;
import seed.seedplusbackend.payment.presentation.dto.RecurringPaymentResponse;
import seed.seedplusbackend.payment.presentation.dto.SubscriptionStatusResponse;

@Tag(name = "구독", description = "구독 API")
public interface SubscriptionApi {

  @Operation(summary = "GMPay 카드 등록 URL 발급", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiErrorCodeExamples({
    ErrorCode.NOT_FOUND_USER,
    ErrorCode.NOT_FOUND_PAYMENT_PRODUCT,
    ErrorCode.INVALID_PAYMENT_ORDER,
    ErrorCode.GMPAY_CARD_REGISTRATION_REQUEST_FAILED,
    ErrorCode.BILLING_KEY_ENCRYPTION_FAILED
  })
  @PostMapping("/card-registrations")
  ResponseEntity<ApiResponse<CardRegistrationResponse>> requestCardRegistration(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Valid @RequestBody CardRegistrationRequest request);

  @Operation(summary = "등록 카드 최초 결제", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiErrorCodeExamples({
    ErrorCode.NOT_FOUND_SUBSCRIPTION,
    ErrorCode.INVALID_SUBSCRIPTION_STATUS,
    ErrorCode.BILLING_KEY_ENCRYPTION_FAILED,
    ErrorCode.GMPAY_RECURRING_PAYMENT_FAILED
  })
  @PostMapping("/{subscriptionId}/payments")
  ResponseEntity<ApiResponse<RecurringPaymentResponse>> requestInitialPayment(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @PathVariable("subscriptionId") Long subscriptionId);

  @Operation(summary = "내 구독 상태 조회", security = @SecurityRequirement(name = "bearerAuth"))
  @GetMapping("/me")
  ResponseEntity<ApiResponse<SubscriptionStatusResponse>> getMySubscription(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser);

  @Operation(summary = "구독 해지", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiErrorCodeExamples({ErrorCode.NOT_FOUND_SUBSCRIPTION, ErrorCode.INVALID_SUBSCRIPTION_STATUS})
  @DeleteMapping("/{subscriptionId}")
  ResponseEntity<ApiResponse<SubscriptionStatusResponse>> cancelSubscription(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @PathVariable("subscriptionId") Long subscriptionId);
}
