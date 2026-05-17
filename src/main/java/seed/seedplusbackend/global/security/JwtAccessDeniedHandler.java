package seed.seedplusbackend.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.global.error.ErrorCode;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

  private final SecurityErrorResponseWriter errorResponseWriter;

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException {
    log.warn(
        "[JwtAccessDeniedHandler] 접근 실패, 사유=권한 부족 path={} method={} 예외={}",
        request.getServletPath(),
        request.getMethod(),
        accessDeniedException.getClass().getSimpleName());
    errorResponseWriter.write(response, ErrorCode.FORBIDDEN);
  }
}
