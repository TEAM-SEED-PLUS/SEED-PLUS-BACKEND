package seed.seedplusbackend.payment.application.result;

public record GmpayPaymentRequestResult(String providerOrderNo, String payUrl) {}
