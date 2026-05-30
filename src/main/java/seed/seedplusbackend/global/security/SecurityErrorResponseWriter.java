package seed.seedplusbackend.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.error.ErrorResponse;

@Component
@RequiredArgsConstructor
public class SecurityErrorResponseWriter {

  private final ObjectMapper objectMapper;

  public void write(HttpServletResponse response, ErrorCode errorCode) throws IOException {
    write(response, errorCode, "");
  }

  public void write(HttpServletResponse response, ErrorCode errorCode, String detail)
      throws IOException {
    response.setStatus(errorCode.getHttpStatus().value());
    response.setCharacterEncoding("UTF-8");
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getWriter(), ErrorResponse.of(errorCode, detail));
  }
}
