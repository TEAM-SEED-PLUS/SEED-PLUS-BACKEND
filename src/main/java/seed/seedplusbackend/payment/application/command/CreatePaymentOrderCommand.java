package seed.seedplusbackend.payment.application.command;

import seed.seedplusbackend.payment.domain.entity.PaymentProductCode;

public record CreatePaymentOrderCommand(PaymentProductCode productCode, String reportId) {}
