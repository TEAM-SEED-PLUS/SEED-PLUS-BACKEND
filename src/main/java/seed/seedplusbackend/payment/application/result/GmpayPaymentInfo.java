package seed.seedplusbackend.payment.application.result;

import java.time.LocalDateTime;

public record GmpayPaymentInfo(
    String providerOrderNo,
    String state,
    long totalPrice,
    String goodName,
    String memberId,
    String payMethod,
    String approvalNo,
    LocalDateTime paidAt) {}
