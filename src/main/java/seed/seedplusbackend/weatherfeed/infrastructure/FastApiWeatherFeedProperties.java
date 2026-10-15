package seed.seedplusbackend.weatherfeed.infrastructure;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "weather-feed.fast-api")
public record FastApiWeatherFeedProperties(@NotBlank String baseUrl) {}
