package seed.seedplusbackend.global.security;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private AccessTokenBlacklist accessTokenBlacklist;
  @Mock private SecurityErrorResponseWriter errorResponseWriter;

  @Test
  @DisplayName("공개 auth 경로는 Authorization 헤더가 있어도 JWT 검증을 스킵한다")
  void doFilter_skipsJwtValidation_whenPublicAuthPathHasAuthorizationHeader()
      throws ServletException, IOException {
    JwtAuthenticationFilter filter =
        new JwtAuthenticationFilter(jwtTokenProvider, accessTokenBlacklist, errorResponseWriter);
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/reissue");
    request.setServletPath("/api/v1/auth/reissue");
    request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer expired-access-token");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, new MockFilterChain());

    verify(jwtTokenProvider, never()).parseAccessClaims("expired-access-token");
    verify(errorResponseWriter, never()).write(response, null);
  }
}
