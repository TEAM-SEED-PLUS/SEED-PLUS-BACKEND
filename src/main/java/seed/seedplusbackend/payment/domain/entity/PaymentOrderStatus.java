package seed.seedplusbackend.payment.domain.entity;

public enum PaymentOrderStatus {
  READY,
  PAYMENT_REQUESTED,
  COMPLETED,
  CANCELED,
  FAILED,
  EXPIRED
}
