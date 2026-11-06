package seed.seedplusbackend.support.infrastructure.mail;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "support.inquiry.mail")
public record InquiryMailProperties(
    @NotBlank @Email String fromAddress, @NotBlank @Email String recipientAddress) {}
