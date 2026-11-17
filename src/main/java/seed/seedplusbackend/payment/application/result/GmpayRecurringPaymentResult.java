package seed.seedplusbackend.payment.application.result;

import java.time.LocalDateTime;

public record GmpayRecurringPaymentResult(
    String providerOrderNo,
    long amount,
    String paymentMethod,
    String approvalNo,
    String receiptUrl,
    LocalDateTime paidAt) {}
