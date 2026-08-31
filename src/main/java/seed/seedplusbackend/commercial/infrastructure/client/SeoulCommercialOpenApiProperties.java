package seed.seedplusbackend.commercial.infrastructure.client;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "seoul.commercial.open-api")
public record SeoulCommercialOpenApiProperties(
    @NotBlank String key,
    @NotBlank String baseUrl,
    @NotBlank String serviceName,
    @NotBlank String type,
    int pageSize,
    long minJitterMillis,
    long maxJitterMillis,
    int maxRetryCount) {}
