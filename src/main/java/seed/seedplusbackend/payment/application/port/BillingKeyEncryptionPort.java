package seed.seedplusbackend.payment.application.port;

public interface BillingKeyEncryptionPort {

  String encrypt(String billingKey);
}
