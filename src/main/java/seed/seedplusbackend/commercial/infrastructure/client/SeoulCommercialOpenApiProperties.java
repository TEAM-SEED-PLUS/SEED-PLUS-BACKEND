package seed.seedplusbackend.commercial.infrastructure.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "seoul.commercial.open-api")
public record SeoulCommercialOpenApiProperties(
    String key,
    String baseUrl,
    String serviceName,
    String type,
    int pageSize,
    long minJitterMillis,
    long maxJitterMillis,
    int maxRetryCount) {}
