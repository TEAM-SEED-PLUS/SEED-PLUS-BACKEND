package seed.seedplusbackend.payment.application.command;

public record GmpayPaymentFeedbackCommand(
    String memberId,
    String feedbackToken,
    String paymentState,
    String orderNo,
    String payMethod,
    String approvalNo,
    String receiptUrl) {}
