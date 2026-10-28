package seed.seedplusbackend.payment.infrastructure.gmpay;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gmpay")
public record GmpayProperties(
    String baseUrl,
    String memberId,
    String feedbackUrl,
    String returnUrl,
    String devId,
    String devToken,
    String feedbackToken) {}
