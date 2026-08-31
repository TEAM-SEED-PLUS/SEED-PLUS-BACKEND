package seed.seedplusbackend.commercial.infrastructure.client;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "seoul.sdot.open-api")
public record SeoulSdotOpenApiProperties(
    @NotBlank String key,
    @NotBlank String baseUrl,
    @NotBlank String serviceName,
    @NotBlank String type,
    @Min(1) @Max(1000) int pageSize,
    @Min(0) long minJitterMillis,
    @Min(0) long maxJitterMillis,
    @Min(0) int maxRetryCount) {}
