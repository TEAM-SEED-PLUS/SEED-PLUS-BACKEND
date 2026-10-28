package seed.seedplusbackend.payment.application.command;

public record GmpayCardRegistrationRequestCommand(
    String goodName,
    String customerMobile,
    String customerNo,
    String customerName,
    String customerEmail,
    String subscriptionProductCode) {}
