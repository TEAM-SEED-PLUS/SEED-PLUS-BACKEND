package seed.seedplusbackend.payment.application;

public record UserEntitlements(UserPlan plan, boolean pro, boolean reportPurchased) {}
