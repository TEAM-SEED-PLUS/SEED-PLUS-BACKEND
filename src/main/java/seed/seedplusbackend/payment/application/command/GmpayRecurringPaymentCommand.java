package seed.seedplusbackend.payment.application.command;

public record GmpayRecurringPaymentCommand(
    String mpKey,
    long totalPrice,
    String goodName,
    String buyerMobile,
    String buyerEmail,
    String buyerName,
    String orderKey,
    String productCode) {}
