package seed.seedplusbackend.payment.application.command;

public record GmpayPaymentRequestCommand(
    String goodName,
    long totalPrice,
    String buyerMobile,
    String buyerEmail,
    String feedbackUrl,
    String returnUrl,
    String orderKey,
    String productCode) {}
