package seed.seedplusbackend.payment.infrastructure.crypto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment.billing-key-encryption")
public record BillingKeyEncryptionProperties(String secret) {}
