package seed.seedplusbackend.payment.infrastructure.scheduler;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment.subscription-renewal")
public record SubscriptionRenewalProperties(boolean enabled, int batchSize) {}
