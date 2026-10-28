package seed.seedplusbackend.payment.application.result;

public record PaymentOrderRequestResult(String orderKey, String providerOrderNo, String payUrl) {}
