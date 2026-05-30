package seed.seedplusbackend.global.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import seed.seedplusbackend.global.security.AuthenticatedUser;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class ApiRequestLoggingFilter extends OncePerRequestFilter {

  public static final String AUTHENTICATED_USER_ID_ATTRIBUTE = "authenticatedUserId";

  private static final String REQUEST_ID_HEADER = "X-Request-Id";
  private static final String REQUEST_ID_MDC_KEY = "requestId";
  private static final int MAX_REQUEST_ID_LENGTH = 64;
  private static final Pattern SENSITIVE_QUERY_PARAMETER_PATTERN =
      Pattern.compile(
          "(?i)(^|&)([^=&]*(?:token|password|secret|authorization|cookie)[^=&]*)=[^&]*");

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getServletPath().startsWith("/api/");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String requestId = resolveRequestId(request);
    long startedAt = System.currentTimeMillis();
    Exception failure = null;

    MDC.put(REQUEST_ID_MDC_KEY, requestId);
    response.setHeader(REQUEST_ID_HEADER, requestId);

    try {
      filterChain.doFilter(request, response);
    } catch (IOException | ServletException | RuntimeException e) {
      failure = e;
      throw e;
    } finally {
      long durationMs = System.currentTimeMillis() - startedAt;
      logCompletedRequest(request, response, durationMs, requestId, failure);
      MDC.remove(REQUEST_ID_MDC_KEY);
    }
  }

  private String resolveRequestId(HttpServletRequest request) {
    String requestId = request.getHeader(REQUEST_ID_HEADER);
    if (!StringUtils.hasText(requestId) || requestId.length() > MAX_REQUEST_ID_LENGTH) {
      return UUID.randomUUID().toString();
    }

    return requestId;
  }

  private void logCompletedRequest(
      HttpServletRequest request,
      HttpServletResponse response,
      long durationMs,
      String requestId,
      Exception failure) {
    int status = failure == null ? response.getStatus() : HttpStatus.INTERNAL_SERVER_ERROR.value();
    String userId = resolveAuthenticatedUserId(request);
    String queryString = sanitizeQueryString(request.getQueryString());

    if (failure != null) {
      log.error(
          "[ApiRequestLoggingFilter] API 요청 실패 method={} path={} query={} status={} 처리시간Ms={} requestId={} clientIp={} 사용자ID={} userAgent={} 예외={}",
          request.getMethod(),
          request.getServletPath(),
          queryString,
          status,
          durationMs,
          requestId,
          resolveClientIp(request),
          userId,
          request.getHeader("User-Agent"),
          failure.getClass().getSimpleName(),
          failure);
      return;
    }

    if (status >= 500) {
      log.error(
          "[ApiRequestLoggingFilter] API 요청 완료, 결과=서버 오류 method={} path={} query={} status={} 처리시간Ms={} requestId={} clientIp={} 사용자ID={} userAgent={}",
          request.getMethod(),
          request.getServletPath(),
          queryString,
          status,
          durationMs,
          requestId,
          resolveClientIp(request),
          userId,
          request.getHeader("User-Agent"));
      return;
    }

    log.info(
        "[ApiRequestLoggingFilter] API 요청 완료 method={} path={} query={} status={} 처리시간Ms={} requestId={} clientIp={} 사용자ID={} userAgent={}",
        request.getMethod(),
        request.getServletPath(),
        queryString,
        status,
        durationMs,
        requestId,
        resolveClientIp(request),
        userId,
        request.getHeader("User-Agent"));
  }

  private String resolveAuthenticatedUserId(HttpServletRequest request) {
    Object userIdAttribute = request.getAttribute(AUTHENTICATED_USER_ID_ATTRIBUTE);
    if (userIdAttribute != null) {
      return String.valueOf(userIdAttribute);
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return "anonymous";
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof AuthenticatedUser authenticatedUser) {
      return String.valueOf(authenticatedUser.getId());
    }

    return "anonymous";
  }

  private String resolveClientIp(HttpServletRequest request) {
    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (StringUtils.hasText(forwardedFor)) {
      return forwardedFor.split(",")[0].trim();
    }

    return request.getRemoteAddr();
  }

  private String sanitizeQueryString(String queryString) {
    if (!StringUtils.hasText(queryString)) {
      return null;
    }

    return SENSITIVE_QUERY_PARAMETER_PATTERN.matcher(queryString).replaceAll("$1$2=***");
  }
}
