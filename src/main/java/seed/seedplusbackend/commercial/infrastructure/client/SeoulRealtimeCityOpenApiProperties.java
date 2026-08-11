package seed.seedplusbackend.commercial.infrastructure.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "seoul.realtime-city.open-api")
public record SeoulRealtimeCityOpenApiProperties(
    @NotBlank String key,
    @NotBlank String baseUrl,
    @NotBlank String serviceName,
    @NotBlank String type,
    @Positive int startIndex,
    @Positive int endIndex,
    long initialBackoffMillis,
    long minJitterMillis,
    long maxJitterMillis,
    int maxRetryCount) {}
