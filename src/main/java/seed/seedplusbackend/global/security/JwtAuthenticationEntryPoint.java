package seed.seedplusbackend.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.global.error.ErrorCode;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final SecurityErrorResponseWriter errorResponseWriter;

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    log.warn(
        "[JwtAuthenticationEntryPoint] 인증 실패, 사유=인증 정보 없음 path={} method={} 예외={}",
        request.getServletPath(),
        request.getMethod(),
        authException.getClass().getSimpleName());
    errorResponseWriter.write(response, ErrorCode.UNAUTHORIZED);
  }
}
