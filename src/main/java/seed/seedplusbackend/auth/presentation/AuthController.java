package seed.seedplusbackend.auth.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seed.seedplusbackend.auth.application.AuthService;
import seed.seedplusbackend.auth.application.AuthTokenResult;
import seed.seedplusbackend.auth.presentation.dto.CsrfTokenResponse;
import seed.seedplusbackend.auth.presentation.dto.LoginRequest;
import seed.seedplusbackend.auth.presentation.dto.SignupRequest;
import seed.seedplusbackend.auth.presentation.dto.TokenResponse;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.security.AuthenticatedUser;
import seed.seedplusbackend.global.swagger.annotation.ApiErrorCodeExamples;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

  private static final String BEARER_PREFIX = "Bearer ";

  private final AuthService authService;
  private final RefreshTokenCookieManager refreshTokenCookieManager;

  @Operation(summary = "CSRF 토큰 발급")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "CSRF 토큰 발급 성공"),
      })
  @GetMapping("/csrf")
  public CsrfTokenResponse csrf(@Parameter(hidden = true) CsrfToken csrfToken) {
    return CsrfTokenResponse.from(csrfToken);
  }

  @Operation(summary = "회원가입", description = "휴대폰 번호 기반 사용자를 생성한다. 성공 응답 body는 없다.")
  @ApiResponse(responseCode = "201", description = "회원가입 성공", content = @Content)
  @ApiErrorCodeExamples({
    ErrorCode.INVALID_PARAMETER,
    ErrorCode.DUPLICATE_PHONE_NUMBER,
    ErrorCode.DUPLICATE_EMAIL
  })
  @PostMapping("/signup")
  public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest request) {
    authService.signup(request.toCommand());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @Operation(
      summary = "로그인",
      description = "Access Token을 응답하고 Refresh Token을 HttpOnly Cookie로 발급한다.")
  @ApiErrorCodeExamples({
    ErrorCode.INVALID_PARAMETER,
    ErrorCode.INVALID_CREDENTIALS,
    ErrorCode.INVALID_USER_STATUS,
    ErrorCode.FORBIDDEN
  })
  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
    AuthTokenResult result = authService.login(request.toCommand());
    return ResponseEntity.ok()
        .header(
            HttpHeaders.SET_COOKIE,
            refreshTokenCookieManager
                .createCookie(result.getRefreshToken(), result.getRefreshTokenExpiresIn())
                .toString())
        .body(TokenResponse.from(result));
  }

  @Operation(summary = "토큰 재발급", description = "Refresh Token Cookie를 검증하고 토큰을 회전 발급한다.")
  @ApiErrorCodeExamples({
    ErrorCode.INVALID_TOKEN,
    ErrorCode.EXPIRED_REFRESH_TOKEN,
    ErrorCode.INVALID_USER_STATUS,
    ErrorCode.FORBIDDEN
  })
  @PostMapping("/reissue")
  public ResponseEntity<TokenResponse> reissue(
      @CookieValue(name = RefreshTokenCookieManager.COOKIE_NAME) String refreshToken) {
    AuthTokenResult result = authService.reissue(refreshToken);
    return ResponseEntity.ok()
        .header(
            HttpHeaders.SET_COOKIE,
            refreshTokenCookieManager
                .createCookie(result.getRefreshToken(), result.getRefreshTokenExpiresIn())
                .toString())
        .body(TokenResponse.from(result));
  }

  @Operation(summary = "로그아웃", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponse(responseCode = "204", description = "로그아웃 성공", content = @Content)
  @ApiErrorCodeExamples({ErrorCode.UNAUTHORIZED, ErrorCode.INVALID_TOKEN})
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
      @CookieValue(name = RefreshTokenCookieManager.COOKIE_NAME, required = false)
          String refreshToken) {
    authService.logout(authenticatedUser, extractBearerToken(authorization), refreshToken);
    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, refreshTokenCookieManager.deleteCookie().toString())
        .build();
  }

  private String extractBearerToken(String authorization) {
    if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
      throw new ApplicationException(ErrorCode.INVALID_TOKEN);
    }

    return authorization.substring(BEARER_PREFIX.length());
  }
}
