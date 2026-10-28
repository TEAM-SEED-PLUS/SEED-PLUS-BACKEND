package seed.seedplusbackend.payment.presentation.dto;

import seed.seedplusbackend.payment.application.command.GmpayPaymentFeedbackCommand;

public record GmpayPaymentFeedbackRequest(
    String memberId,
    String feedbackToken,
    String paymentState,
    String orderNo,
    String payMethod,
    String approvalNo,
    String receiptUrl) {

  public GmpayPaymentFeedbackCommand toCommand() {
    return new GmpayPaymentFeedbackCommand(
        memberId, feedbackToken, paymentState, orderNo, payMethod, approvalNo, receiptUrl);
  }
}
