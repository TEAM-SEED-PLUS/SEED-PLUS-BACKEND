package seed.seedplusbackend.global.error;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApplicationException.class)
  public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException e) {
    ErrorCode errorCode = e.getErrorCode();
    logCustomException("ApplicationException", errorCode, e.getDetail(), e);
    return createErrorResponse(errorCode, e.getDetail());
  }

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ErrorResponse> handleDomainException(DomainException e) {
    ErrorCode errorCode = e.getErrorCode();
    logCustomException("DomainException", errorCode, e.getDetail(), e);
    return createErrorResponse(errorCode, e.getDetail());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e) {
    String detail = getBindingErrorDetail(e);
    log.warn("[GlobalExceptionHandler] 요청 본문 검증 실패, 사유={}", detail);
    return createErrorResponse(ErrorCode.INVALID_PARAMETER, detail);
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<ErrorResponse> handleBindException(BindException e) {
    String detail = getBindingErrorDetail(e);
    log.warn("[GlobalExceptionHandler] 파라미터 바인딩 검증 실패, 사유={}", detail);
    return createErrorResponse(ErrorCode.INVALID_PARAMETER, detail);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolationException(
      ConstraintViolationException e) {
    String path = getFirstConstraintPath(e);
    String detail = getConstraintViolationDetail(e);
    ErrorCode errorCode = resolveParameterErrorCode(path);

    log.warn("[GlobalExceptionHandler] 제약 조건 검증 실패, 경로={} 사유={}", path, detail);
    return createErrorResponse(errorCode, detail);
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(
      HandlerMethodValidationException e) {
    String detail = "메서드 파라미터 검증 실패";
    log.warn("[GlobalExceptionHandler] 메서드 파라미터 검증 실패, 사유={}", detail);
    return createErrorResponse(ErrorCode.INVALID_PARAMETER, detail);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException e) {
    log.warn("[GlobalExceptionHandler] 요청 본문 파싱 실패, 사유={}", e.getMessage());
    return createErrorResponse(ErrorCode.INVALID_REQUEST);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException e) {
    ErrorCode errorCode = resolveParameterErrorCode(e.getName());
    String detail =
        "parameter=%s, value=%s, requiredType=%s"
            .formatted(e.getName(), e.getValue(), getRequiredTypeName(e));

    log.warn("[GlobalExceptionHandler] 파라미터 타입 변환 실패, 사유={}", detail);
    return createErrorResponse(errorCode, detail);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
      MissingServletRequestParameterException e) {
    String detail = "parameter=%s".formatted(e.getParameterName());
    log.warn("[GlobalExceptionHandler] 필수 요청 파라미터 누락, 사유={}", detail);
    return createErrorResponse(ErrorCode.MISSING_REQUIRED_PARAMETER, detail);
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(
      MissingRequestHeaderException e) {
    String detail = "header=%s".formatted(e.getHeaderName());
    log.warn("[GlobalExceptionHandler] 필수 요청 헤더 누락, 사유={}", detail);
    return createErrorResponse(ErrorCode.MISSING_REQUIRED_HEADER, detail);
  }

  @ExceptionHandler(MissingRequestCookieException.class)
  public ResponseEntity<ErrorResponse> handleMissingRequestCookieException(
      MissingRequestCookieException e) {
    String detail = "cookie=%s".formatted(e.getCookieName());
    log.warn("[GlobalExceptionHandler] 필수 요청 쿠키 누락, 사유={}", detail);
    return createErrorResponse(ErrorCode.INVALID_TOKEN, detail);
  }

  @ExceptionHandler(MissingPathVariableException.class)
  public ResponseEntity<ErrorResponse> handleMissingPathVariableException(
      MissingPathVariableException e) {
    String detail = "pathVariable=%s".formatted(e.getVariableName());
    log.warn("[GlobalExceptionHandler] 경로 변수 누락, 사유={}", detail);
    return createErrorResponse(ErrorCode.INVALID_PARAMETER, detail);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
      HttpRequestMethodNotSupportedException e) {
    log.warn(
        "[GlobalExceptionHandler] 지원하지 않는 HTTP 메서드, method={} supported={}",
        e.getMethod(),
        e.getSupportedMethods());
    return createErrorResponse(ErrorCode.METHOD_NOT_ALLOWED);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
      HttpMediaTypeNotSupportedException e) {
    log.warn(
        "[GlobalExceptionHandler] 지원하지 않는 미디어 타입, contentType={} supported={}",
        e.getContentType(),
        e.getSupportedMediaTypes());
    return createErrorResponse(ErrorCode.UNSUPPORTED_MEDIA_TYPE);
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException e) {
    String detail = "%s %s".formatted(e.getHttpMethod(), e.getRequestURL());
    log.warn("[GlobalExceptionHandler] 요청 리소스 조회 실패, 사유=핸들러 없음 detail={}", detail);
    return createErrorResponse(ErrorCode.RESOURCE_NOT_FOUND, detail);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException e) {
    String detail = "%s %s".formatted(e.getHttpMethod(), e.getResourcePath());
    log.warn("[GlobalExceptionHandler] 요청 리소스 조회 실패, 사유=정적 리소스 없음 detail={}", detail);
    return createErrorResponse(ErrorCode.RESOURCE_NOT_FOUND, detail);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
      DataIntegrityViolationException e) {
    log.warn(
        "[GlobalExceptionHandler] 데이터 저장 실패, 사유=데이터 무결성 제약 위반 detail={}",
        e.getMostSpecificCause().getMessage());
    return createErrorResponse(ErrorCode.DATA_INTEGRITY_VIOLATION);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
    log.warn("[GlobalExceptionHandler] 잘못된 요청 처리 실패, 사유={}", e.getMessage(), e);
    return createErrorResponse(ErrorCode.INVALID_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception e) {
    log.error("[GlobalExceptionHandler] 처리되지 않은 서버 예외 발생, 사유={}", e.getMessage(), e);
    return createErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR);
  }

  private ResponseEntity<ErrorResponse> createErrorResponse(ErrorCode errorCode) {
    return ResponseEntity.status(errorCode.getHttpStatus()).body(ErrorResponse.of(errorCode));
  }

  private ResponseEntity<ErrorResponse> createErrorResponse(ErrorCode errorCode, String detail) {
    return ResponseEntity.status(errorCode.getHttpStatus())
        .body(ErrorResponse.of(errorCode, detail));
  }

  private void logCustomException(
      String exceptionName, ErrorCode errorCode, String detail, RuntimeException e) {
    if (errorCode.getHttpStatus().is5xxServerError()) {
      log.error(
          "[GlobalExceptionHandler] 커스텀 예외 발생, 예외={} code={} 메시지={} 상세={}",
          exceptionName,
          errorCode.getCode(),
          errorCode.getMessage(),
          detail,
          e);
      return;
    }

    log.warn(
        "[GlobalExceptionHandler] 커스텀 예외 발생, 예외={} code={} 메시지={} 상세={}",
        exceptionName,
        errorCode.getCode(),
        errorCode.getMessage(),
        detail);
  }

  private String getBindingErrorDetail(BindException e) {
    FieldError fieldError = e.getBindingResult().getFieldError();
    if (fieldError != null) {
      return "%s: %s".formatted(fieldError.getField(), fieldError.getDefaultMessage());
    }

    ObjectError globalError = e.getBindingResult().getGlobalError();
    if (globalError != null) {
      return globalError.getDefaultMessage();
    }

    return e.getMessage();
  }

  private String getFirstConstraintPath(ConstraintViolationException e) {
    return e.getConstraintViolations().stream()
        .findFirst()
        .map(violation -> violation.getPropertyPath().toString())
        .orElse("");
  }

  private String getConstraintViolationDetail(ConstraintViolationException e) {
    return e.getConstraintViolations().stream()
        .findFirst()
        .map(violation -> "%s: %s".formatted(violation.getPropertyPath(), violation.getMessage()))
        .orElse(e.getMessage());
  }

  private ErrorCode resolveParameterErrorCode(String parameterName) {
    if (parameterName == null) {
      return ErrorCode.INVALID_PARAMETER;
    }

    if (parameterName.contains("size")) {
      return ErrorCode.INVALID_SIZE;
    }
    if (parameterName.contains("lastId")) {
      return ErrorCode.INVALID_LAST_ID;
    }
    if (parameterName.contains("lastPrice")) {
      return ErrorCode.INVALID_LAST_PRICE;
    }
    if (parameterName.contains("lastOrderCount")) {
      return ErrorCode.INVALID_LAST_ORDER_COUNT;
    }
    if (parameterName.contains("lastSellingStatus")) {
      return ErrorCode.INVALID_LAST_SELLING_STATUS;
    }
    if (parameterName.contains("sort")) {
      return ErrorCode.INVALID_SORT;
    }

    return ErrorCode.INVALID_PARAMETER;
  }

  private String getRequiredTypeName(MethodArgumentTypeMismatchException e) {
    Class<?> requiredType = e.getRequiredType();
    return requiredType == null ? "unknown" : requiredType.getSimpleName();
  }
}
