package seed.seedplusbackend.global.error;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
public enum ErrorCode {

  // 1000: Success
  SUCCESS(HttpStatus.OK, 1000, "정상적인 요청입니다."),
  CREATED(HttpStatus.CREATED, 1001, "정상적으로 생성되었습니다."),

  // 2000: Common
  INVALID_REQUEST(HttpStatus.BAD_REQUEST, 2000, "잘못된 요청입니다."),
  INVALID_PARAMETER(HttpStatus.BAD_REQUEST, 2001, "잘못된 파라미터입니다."),
  INVALID_SIZE(HttpStatus.BAD_REQUEST, 2002, "size 파라미터가 올바르지 않습니다."),
  INVALID_SORT(HttpStatus.BAD_REQUEST, 2003, "sort 파라미터가 올바르지 않습니다."),
  INVALID_LAST_ID(HttpStatus.BAD_REQUEST, 2004, "lastId 파라미터가 올바르지 않습니다."),
  INVALID_LAST_PRICE(HttpStatus.BAD_REQUEST, 2005, "lastPrice 파라미터가 올바르지 않습니다."),
  INVALID_LAST_ORDER_COUNT(HttpStatus.BAD_REQUEST, 2006, "lastOrderCount 파라미터가 올바르지 않습니다."),
  INVALID_LAST_SELLING_STATUS(HttpStatus.BAD_REQUEST, 2007, "lastSellingStatus 파라미터가 올바르지 않습니다."),
  MISSING_REQUIRED_PARAMETER(HttpStatus.BAD_REQUEST, 2008, "필수 요청 파라미터가 누락되었습니다."),
  MISSING_REQUIRED_HEADER(HttpStatus.BAD_REQUEST, 2009, "필수 요청 헤더가 누락되었습니다."),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, 2010, "지원하지 않는 HTTP 메서드입니다."),
  UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, 2011, "지원하지 않는 미디어 타입입니다."),
  RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, 2012, "요청한 리소스를 찾을 수 없습니다."),
  DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, 2013, "데이터 무결성 제약 조건을 위반했습니다."),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 2100, "인증이 필요합니다."),
  FORBIDDEN(HttpStatus.FORBIDDEN, 2200, "접근 권한이 없습니다."),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 2900, "서버 내부 오류가 발생했습니다."),

  // 3000: Auth
  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, 3000, "이메일 또는 비밀번호가 올바르지 않습니다."),
  EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, 3001, "토큰이 만료되었습니다."),
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, 3002, "유효하지 않은 토큰입니다."),
  EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, 3003, "리프레시 토큰이 만료되었습니다."),

  // 4000: User
  NOT_FOUND_USER(HttpStatus.NOT_FOUND, 4000, "사용자를 찾을 수 없습니다."),
  DUPLICATE_EMAIL(HttpStatus.CONFLICT, 4001, "이미 사용 중인 이메일입니다."),
  INVALID_USER_STATUS(HttpStatus.BAD_REQUEST, 4002, "유효하지 않은 사용자 상태입니다."),

  // 5000: Region
  NOT_FOUND_REGION(HttpStatus.NOT_FOUND, 5000, "지역을 찾을 수 없습니다."),

  // 5100: Industry
  NOT_FOUND_INDUSTRY(HttpStatus.NOT_FOUND, 5100, "업종을 찾을 수 없습니다."),

  // 5200: Commercial Area
  NOT_FOUND_COMMERCIAL_AREA(HttpStatus.NOT_FOUND, 5200, "상권을 찾을 수 없습니다."),

  // 5300: Building
  NOT_FOUND_BUILDING(HttpStatus.NOT_FOUND, 5300, "건물을 찾을 수 없습니다."),

  // 6000: Store
  NOT_FOUND_STORE(HttpStatus.NOT_FOUND, 6000, "점포를 찾을 수 없습니다."),
  INVALID_STORE_STATUS(HttpStatus.BAD_REQUEST, 6001, "유효하지 않은 점포 상태입니다."),

  // 7000: Builder Store
  NOT_FOUND_BUILDER_STORE(HttpStatus.NOT_FOUND, 7000, "가상 점포를 찾을 수 없습니다."),
  NOT_OWNER_BUILDER_STORE(HttpStatus.FORBIDDEN, 7001, "가상 점포의 소유자가 아닙니다."),
  ALREADY_LIKED(HttpStatus.CONFLICT, 7002, "이미 좋아요한 점포입니다."),
  NOT_LIKED(HttpStatus.BAD_REQUEST, 7003, "좋아요하지 않은 점포입니다."),
  ALREADY_BOOKMARKED(HttpStatus.CONFLICT, 7004, "이미 북마크한 점포입니다."),
  NOT_BOOKMARKED(HttpStatus.BAD_REQUEST, 7005, "북마크하지 않은 점포입니다."),
  INVALID_BUILDER_STORE_STATUS(HttpStatus.BAD_REQUEST, 7006, "유효하지 않은 가상 점포 상태입니다."),

  // 7100: Comment
  NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND, 7100, "댓글을 찾을 수 없습니다."),
  NOT_OWNER_COMMENT(HttpStatus.FORBIDDEN, 7101, "댓글 작성자가 아닙니다."),
  INVALID_PARENT_COMMENT(HttpStatus.BAD_REQUEST, 7102, "유효하지 않은 부모 댓글입니다."),

  // 8000: Score
  NOT_FOUND_SCORE(HttpStatus.NOT_FOUND, 8000, "점수 정보를 찾을 수 없습니다."),

  // 9000: Metrics
  NOT_FOUND_METRIC(HttpStatus.NOT_FOUND, 9000, "메트릭 데이터를 찾을 수 없습니다."),
  ;

  private final HttpStatus httpStatus;
  private final Integer code;
  private final String message;

  ErrorCode(HttpStatus httpStatus, Integer code, String message) {
    this.httpStatus = httpStatus;
    this.code = code;
    this.message = message;
  }
}
