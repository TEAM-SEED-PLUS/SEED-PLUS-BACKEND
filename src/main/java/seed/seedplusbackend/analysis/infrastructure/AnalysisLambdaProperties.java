package seed.seedplusbackend.analysis.infrastructure;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "analysis.lambda")
public record AnalysisLambdaProperties(
    @Valid @NotNull FunctionProperties profit, @Valid @NotNull FunctionProperties survival) {

  public record FunctionProperties(@NotBlank String endpoint, @NotBlank String apiKey) {}
}
