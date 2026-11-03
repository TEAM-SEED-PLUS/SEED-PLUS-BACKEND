package seed.seedplusbackend.payment.application.command;

public record CreateRefundRequestCommand(Long paymentId, String reason) {}
