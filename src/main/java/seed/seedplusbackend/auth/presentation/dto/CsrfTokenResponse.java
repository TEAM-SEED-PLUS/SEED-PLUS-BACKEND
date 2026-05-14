package seed.seedplusbackend.auth.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.web.csrf.CsrfToken;

@Getter
@Builder
@Schema(description = "CSRF 토큰 응답")
public class CsrfTokenResponse {

  @Schema(description = "CSRF 헤더 이름", example = "X-XSRF-TOKEN")
  private final String headerName;

  @Schema(description = "CSRF 파라미터 이름", example = "_csrf")
  private final String parameterName;

  @Schema(description = "CSRF 토큰 값")
  private final String token;

  public static CsrfTokenResponse from(CsrfToken csrfToken) {
    return CsrfTokenResponse.builder()
        .headerName(csrfToken.getHeaderName())
        .parameterName(csrfToken.getParameterName())
        .token(csrfToken.getToken())
        .build();
  }
}
