package seed.seedplusbackend.auth.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.security.web.csrf.CsrfToken;

@Schema(description = "CSRF 토큰 응답")
public record CsrfTokenResponse(
    @Schema(description = "CSRF 헤더 이름", example = "X-XSRF-TOKEN") String headerName,
    @Schema(description = "CSRF 파라미터 이름", example = "_csrf") String parameterName,
    @Schema(description = "CSRF 토큰 값") String token) {

  public static CsrfTokenResponse from(CsrfToken csrfToken) {
    return new CsrfTokenResponse(
        csrfToken.getHeaderName(), csrfToken.getParameterName(), csrfToken.getToken());
  }
}
