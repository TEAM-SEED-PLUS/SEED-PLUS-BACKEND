package seed.seedplusbackend.auth.infrastructure.mail;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "auth.temporary-password.mail")
public record TemporaryPasswordMailProperties(@NotBlank String fromAddress) {}
