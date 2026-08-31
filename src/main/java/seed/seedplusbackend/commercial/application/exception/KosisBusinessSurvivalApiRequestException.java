package seed.seedplusbackend.commercial.application.exception;

import lombok.Getter;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@Getter
public class KosisBusinessSurvivalApiRequestException extends ApplicationException {

  private final boolean retryable;

  public KosisBusinessSurvivalApiRequestException(boolean retryable) {
    super(ErrorCode.KOSIS_OPEN_API_REQUEST_FAILED);
    this.retryable = retryable;
  }

  public KosisBusinessSurvivalApiRequestException(boolean retryable, Throwable cause) {
    super(ErrorCode.KOSIS_OPEN_API_REQUEST_FAILED, cause);
    this.retryable = retryable;
  }
}
