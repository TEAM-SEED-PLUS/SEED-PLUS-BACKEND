package seed.seedplusbackend.commercial.infrastructure.client;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "kosis.business-survival.open-api")
public record KosisBusinessSurvivalOpenApiProperties(
    @NotBlank String key,
    @NotBlank String baseUrl,
    @NotBlank String endpoint,
    @NotBlank String organizationId,
    @NotBlank String tableId,
    @Min(0) long initialBackoffMillis,
    @Min(0) long minJitterMillis,
    @Min(0) long maxJitterMillis,
    @Min(0) int maxRetryCount) {}
