package seed.seedplusbackend.commercial.infrastructure.client;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "small-business.store.open-api")
public record SmallBusinessStoreOpenApiProperties(
    @NotBlank String serviceKey,
    @NotBlank String baseUrl,
    @NotBlank String endpoint,
    @NotBlank String type,
    @Min(1) @Max(1000) int pageSize,
    @Min(0) long initialBackoffMillis,
    @Min(0) @Max(5) int maxRetryCount) {}
