package seed.seedplusbackend.payment.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.response.PageResponse;
import seed.seedplusbackend.global.security.AuthenticatedUser;
import seed.seedplusbackend.global.swagger.annotation.ApiErrorCodeExamples;
import seed.seedplusbackend.payment.presentation.dto.CreatePaymentOrderRequest;
import seed.seedplusbackend.payment.presentation.dto.CreateRefundRequest;
import seed.seedplusbackend.payment.presentation.dto.PaymentHistoryResponse;
import seed.seedplusbackend.payment.presentation.dto.PaymentOrderRequestResponse;
import seed.seedplusbackend.payment.presentation.dto.RefundRequestResponse;

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

  @Operation(hidden = true)
  @PostMapping("/gmpay/feedback")
  ResponseEntity<ApiResponse<Void>> handleGmpayFeedback(
      @RequestParam String memberId,
      @RequestParam String feedbackToken,
      @RequestParam String paymentState,
      @RequestParam String orderNo,
      @RequestParam(required = false) String payMethod,
      @RequestParam(required = false) String approvalNo,
      @RequestParam(required = false, name = "cstUrl") String receiptUrl);

  @Operation(summary = "내 결제내역 조회", security = @SecurityRequirement(name = "bearerAuth"))
  @GetMapping("/history")
  ResponseEntity<ApiResponse<PageResponse<PaymentHistoryResponse>>> getPaymentHistory(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size);

  @Operation(summary = "환불 신청", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiErrorCodeExamples({ErrorCode.INVALID_REFUNDABLE_PAYMENT, ErrorCode.DUPLICATE_REFUND_REQUEST})
  @PostMapping("/refund-requests")
  ResponseEntity<ApiResponse<RefundRequestResponse>> createRefundRequest(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Valid @RequestBody CreateRefundRequest request);

  @Operation(summary = "내 환불 신청 내역 조회", security = @SecurityRequirement(name = "bearerAuth"))
  @GetMapping("/refund-requests")
  ResponseEntity<ApiResponse<List<RefundRequestResponse>>> getMyRefundRequests(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser);
}
