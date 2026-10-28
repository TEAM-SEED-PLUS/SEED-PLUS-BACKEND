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
import seed.seedplusbackend.payment.application.SubscriptionCardRegistrationService;
import seed.seedplusbackend.payment.presentation.dto.CardRegistrationRequest;
import seed.seedplusbackend.payment.presentation.dto.CardRegistrationResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController implements SubscriptionApi {

  private final SubscriptionCardRegistrationService subscriptionCardRegistrationService;

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
}
