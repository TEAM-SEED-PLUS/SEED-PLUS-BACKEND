package seed.seedplusbackend.commercial.application.exception;

import lombok.Getter;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@Getter
public class SmallBusinessStoreApiRequestException extends ApplicationException {

  private final boolean retryable;

  public SmallBusinessStoreApiRequestException(boolean retryable) {
    super(ErrorCode.SMALL_BUSINESS_STORE_API_REQUEST_FAILED);
    this.retryable = retryable;
  }

  public SmallBusinessStoreApiRequestException(boolean retryable, Throwable cause) {
    super(ErrorCode.SMALL_BUSINESS_STORE_API_REQUEST_FAILED, cause);
    this.retryable = retryable;
  }
}
