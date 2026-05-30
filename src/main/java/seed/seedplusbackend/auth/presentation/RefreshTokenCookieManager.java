package seed.seedplusbackend.auth.presentation;

import java.time.Duration;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieManager {

  public static final String COOKIE_NAME = "refreshToken";

  public ResponseCookie createCookie(String refreshToken, long maxAgeMillis) {
    return baseCookie(refreshToken).maxAge(Duration.ofMillis(maxAgeMillis)).build();
  }

  public ResponseCookie deleteCookie() {
    return baseCookie("").maxAge(Duration.ZERO).build();
  }

  private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
    return ResponseCookie.from(COOKIE_NAME, value)
        .httpOnly(true)
        .secure(true)
        .sameSite("None")
        .path("/api/v1/auth");
  }
}
