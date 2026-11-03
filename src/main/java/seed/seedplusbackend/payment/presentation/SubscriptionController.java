package seed.seedplusbackend.payment.presentation;

import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.security.AuthenticatedUser;
import seed.seedplusbackend.payment.application.SubscriptionCardRegistrationService;
import seed.seedplusbackend.payment.application.SubscriptionCancellationService;
import seed.seedplusbackend.payment.application.SubscriptionRecurringPaymentService;
import seed.seedplusbackend.payment.application.SubscriptionQueryService;
import seed.seedplusbackend.payment.presentation.dto.CardRegistrationRequest;
import seed.seedplusbackend.payment.presentation.dto.CardRegistrationResponse;
import seed.seedplusbackend.payment.presentation.dto.RecurringPaymentResponse;
import seed.seedplusbackend.payment.presentation.dto.SubscriptionStatusResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController implements SubscriptionApi {

  private final SubscriptionCardRegistrationService subscriptionCardRegistrationService;
  private final SubscriptionCancellationService subscriptionCancellationService;
  private final SubscriptionRecurringPaymentService subscriptionRecurringPaymentService;
  private final SubscriptionQueryService subscriptionQueryService;

  @Override
  public ResponseEntity<ApiResponse<CardRegistrationResponse>> requestCardRegistration(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Valid @RequestBody CardRegistrationRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            CardRegistrationResponse.from(
                subscriptionCardRegistrationService.requestCardRegistration(
                    authenticatedUser.getId(), request.toCommand()))));
  }

  @Override
  public ResponseEntity<ApiResponse<RecurringPaymentResponse>> requestInitialPayment(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @PathVariable("subscriptionId") Long subscriptionId) {
    return ResponseEntity.ok(
        ApiResponse.success(
            RecurringPaymentResponse.from(
                subscriptionRecurringPaymentService.requestInitialPayment(
                    authenticatedUser.getId(), subscriptionId))));
  }

  @Override
  public ResponseEntity<ApiResponse<SubscriptionStatusResponse>> getMySubscription(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
    return ResponseEntity.ok(
        ApiResponse.success(
            SubscriptionStatusResponse.from(
                subscriptionQueryService.getLatestSubscription(
                    authenticatedUser.getId(), OffsetDateTime.now()))));
  }

  @Override
  public ResponseEntity<ApiResponse<SubscriptionStatusResponse>> cancelSubscription(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @PathVariable("subscriptionId") Long subscriptionId) {
    return ResponseEntity.ok(
        ApiResponse.success(
            SubscriptionStatusResponse.from(
                subscriptionCancellationService.cancel(
                    authenticatedUser.getId(), subscriptionId, OffsetDateTime.now()))));
  }
}
