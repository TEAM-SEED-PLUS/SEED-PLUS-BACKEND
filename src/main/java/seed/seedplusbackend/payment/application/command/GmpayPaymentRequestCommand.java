package seed.seedplusbackend.payment.application.command;

public record GmpayPaymentRequestCommand(
    String goodName,
    long totalPrice,
    String buyerMobile,
    String buyerEmail,
    String orderKey,
    String productCode) {}
