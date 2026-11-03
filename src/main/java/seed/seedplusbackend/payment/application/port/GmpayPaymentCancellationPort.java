package seed.seedplusbackend.payment.application.port;

public interface GmpayPaymentCancellationPort {

  void cancelPayment(String providerOrderNo);
}
