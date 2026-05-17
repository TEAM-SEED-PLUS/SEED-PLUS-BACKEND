package seed.seedplusbackend.global.response;

import org.springframework.http.HttpStatus;

public record ApiResponse<T>(int status, int code, String message, T data) {

  private static final int DEFAULT_SUCCESS_CODE = 2000;
  private static final String DEFAULT_SUCCESS_MESSAGE = "요청 성공";

  public static <T> ApiResponse<T> success(T data) {
    return success(HttpStatus.OK, data);
  }

  public static <T> ApiResponse<T> success(HttpStatus status, T data) {
    return new ApiResponse<>(status.value(), DEFAULT_SUCCESS_CODE, DEFAULT_SUCCESS_MESSAGE, data);
  }

  public static ApiResponse<Void> success(HttpStatus status) {
    return success(status, null);
  }
}
