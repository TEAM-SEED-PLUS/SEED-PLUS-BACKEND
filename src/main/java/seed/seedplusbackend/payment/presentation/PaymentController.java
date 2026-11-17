package seed.seedplusbackend.payment.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.response.PageResponse;
import seed.seedplusbackend.global.security.AuthenticatedUser;
import seed.seedplusbackend.payment.application.PaymentFeedbackService;
import seed.seedplusbackend.payment.application.PaymentHistoryQueryService;
import seed.seedplusbackend.payment.application.PaymentOrderService;
import seed.seedplusbackend.payment.application.RefundRequestService;
import seed.seedplusbackend.payment.presentation.dto.CreatePaymentOrderRequest;
import seed.seedplusbackend.payment.presentation.dto.CreateRefundRequest;
import seed.seedplusbackend.payment.presentation.dto.GmpayPaymentFeedbackRequest;
import seed.seedplusbackend.payment.presentation.dto.PaymentHistoryResponse;
import seed.seedplusbackend.payment.presentation.dto.PaymentOrderRequestResponse;
import seed.seedplusbackend.payment.presentation.dto.RefundRequestResponse;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController implements PaymentApi {

  private final PaymentOrderService paymentOrderService;
  private final PaymentFeedbackService paymentFeedbackService;
  private final PaymentHistoryQueryService paymentHistoryQueryService;
  private final RefundRequestService refundRequestService;

  @Override
  public ResponseEntity<ApiResponse<PaymentOrderRequestResponse>> requestPayment(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Valid @RequestBody CreatePaymentOrderRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            PaymentOrderRequestResponse.from(
                paymentOrderService.requestPayment(
                    authenticatedUser.getId(), request.toCommand()))));
  }

  @Override
  public ResponseEntity<ApiResponse<Void>> handleGmpayFeedback(
      @RequestParam String memberId,
      @RequestParam String feedbackToken,
      @RequestParam String paymentState,
      @RequestParam String orderNo,
      @RequestParam(required = false) String payMethod,
      @RequestParam(required = false) String approvalNo,
      @RequestParam(required = false, name = "cstUrl") String receiptUrl) {
    paymentFeedbackService.handleFeedback(
        new GmpayPaymentFeedbackRequest(
                memberId, feedbackToken, paymentState, orderNo, payMethod, approvalNo, receiptUrl)
            .toCommand());
    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
  }

  @Override
  public ResponseEntity<ApiResponse<PageResponse<PaymentHistoryResponse>>> getPaymentHistory(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    return ResponseEntity.ok(
        ApiResponse.success(
            PageResponse.from(
                paymentHistoryQueryService.getPaymentHistory(authenticatedUser.getId(), page, size),
                PaymentHistoryResponse::from)));
  }

  @Override
  public ResponseEntity<ApiResponse<RefundRequestResponse>> createRefundRequest(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Valid @RequestBody CreateRefundRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            RefundRequestResponse.from(
                refundRequestService.create(authenticatedUser.getId(), request.toCommand()))));
  }

  @Override
  public ResponseEntity<ApiResponse<List<RefundRequestResponse>>> getMyRefundRequests(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
    return ResponseEntity.ok(
        ApiResponse.success(
            refundRequestService.getMyRefundRequests(authenticatedUser.getId()).stream()
                .map(RefundRequestResponse::from)
                .toList()));
  }
}
