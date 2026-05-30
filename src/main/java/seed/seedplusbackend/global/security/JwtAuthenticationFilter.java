package seed.seedplusbackend.global.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.logging.ApiRequestLoggingFilter;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";
  private static final Set<String> PUBLIC_AUTH_PATHS =
      Set.of(
          "/api/v1/auth/csrf", "/api/v1/auth/signup", "/api/v1/auth/login", "/api/v1/auth/reissue");

  private final JwtTokenProvider jwtTokenProvider;
  private final AccessTokenBlacklist accessTokenBlacklist;
  private final SecurityErrorResponseWriter errorResponseWriter;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return PUBLIC_AUTH_PATHS.contains(request.getServletPath());
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String accessToken = resolveAccessToken(request);
    if (!StringUtils.hasText(accessToken)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      Claims claims = jwtTokenProvider.parseAccessClaims(accessToken);
      String jti = jwtTokenProvider.getJti(claims);
      if (accessTokenBlacklist.contains(jti)) {
        log.warn(
            "[JwtAuthenticationFilter] 액세스 토큰 인증 실패, 사유=블랙리스트 토큰 path={} method={}",
            request.getServletPath(),
            request.getMethod());
        errorResponseWriter.write(response, ErrorCode.INVALID_TOKEN);
        return;
      }

      Authentication authentication = jwtTokenProvider.toAuthentication(claims);
      SecurityContextHolder.getContext().setAuthentication(authentication);
      request.setAttribute(
          ApiRequestLoggingFilter.AUTHENTICATED_USER_ID_ATTRIBUTE,
          ((AuthenticatedUser) authentication.getPrincipal()).getId());
      filterChain.doFilter(request, response);
    } catch (ApplicationException e) {
      SecurityContextHolder.clearContext();
      log.warn(
          "[JwtAuthenticationFilter] 액세스 토큰 인증 실패, 사유=유효하지 않은 토큰 path={} method={} code={} detail={}",
          request.getServletPath(),
          request.getMethod(),
          e.getErrorCode().getCode(),
          e.getDetail());
      errorResponseWriter.write(response, e.getErrorCode(), e.getDetail());
    }
  }

  private String resolveAccessToken(HttpServletRequest request) {
    String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
      return null;
    }

    return authorization.substring(BEARER_PREFIX.length());
  }
}
