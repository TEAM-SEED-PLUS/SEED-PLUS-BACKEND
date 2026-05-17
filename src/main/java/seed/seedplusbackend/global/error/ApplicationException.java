package seed.seedplusbackend.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ApplicationException extends RuntimeException {

  private final ErrorCode errorCode;
  private final String detail;

  public ApplicationException(ErrorCode errorCode) {
    this.errorCode = errorCode;
    this.detail = "";
  }

  public ApplicationException(ErrorCode errorCode, Throwable cause) {
    super(cause);
    this.errorCode = errorCode;
    this.detail = "";
  }

  public ApplicationException(ErrorCode errorCode, String detail, Throwable cause) {
    super(cause);
    this.errorCode = errorCode;
    this.detail = detail;
  }

  @Override
  public String getMessage() {
    if (detail == null || detail.isBlank()) {
      return errorCode.getMessage();
    }

    return "%s detail=%s".formatted(errorCode.getMessage(), detail);
  }
}
