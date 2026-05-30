---
description: ErrorCode 기반 예외 처리, ApplicationException/DomainException 사용 기준, ErrorResponse 포맷, Swagger 에러 명세. 예외 클래스 작성, 컨트롤러 에러 응답, GlobalExceptionHandler 작업, @ApiErrorCodeExamples 명세 시 참조한다.
globs: **/*Exception.java, **/error/**, **/exception/**, **/*Controller.java, **/GlobalExceptionHandler.java
alwaysApply: false
---

# Error Handling & Swagger Convention

예외 처리는 `ErrorCode` 기반으로 수행한다.

---

## 1. 구조

| 구성 요소 | 설명 |
| --- | --- |
| ErrorCode | HTTP 상태 + 코드 + 메시지 정의 |
| ApplicationException | 서비스/흐름 에러 |
| DomainException | 도메인 규칙 위반 |
| ErrorResponse | API 에러 응답 |
| GlobalExceptionHandler | 예외를 응답으로 변환 |

---

## 2. 동작 흐름

```text
Exception 발생
 → ApplicationException / DomainException
 → GlobalExceptionHandler
 → ErrorResponse 반환
```

### 기본 원칙

- 모든 예외는 ErrorCode 기반으로 처리한다.
- `throw new RuntimeException()` 사용을 금지한다.
- Controller에서 예외 응답을 직접 조립하지 않는다.
- 예외 응답 변환은 GlobalExceptionHandler에서 처리한다.
- 도메인 규칙 위반은 DomainException을 사용한다.
- 조회 실패, 권한, 유스케이스 흐름 오류는 ApplicationException을 사용한다.

---

## 3. ErrorResponse Format

최종 에러 응답은 다음 형태를 따른다.

```json
{
  "status": 400,
  "code": 4001,
  "message": "에러 메시지",
  "detail": "상세 설명"
}
```

| Field | Description |
| --- | --- |
| status | HTTP 상태 코드 |
| code | 서비스 커스텀 에러 코드 |
| message | 사용자 또는 클라이언트에 전달할 에러 메시지 |
| detail | 상세 설명 |

---

## 4. Exception 사용 기준

| 상황 | 예외 |
| --- | --- |
| 비즈니스 규칙 위반 | DomainException |
| 조회 실패 | ApplicationException |
| 권한 오류 | ApplicationException |
| 유스케이스 흐름 오류 | ApplicationException |

### 사용 예시

```java
throw new ApplicationException(ErrorCode.NOT_FOUND_USER);
```

```java
throw new DomainException(ErrorCode.INVALID_STATUS, "현재 상태: CLOSED");
```

---

## 5. Response Convention

반환 응답은 커스텀 응답 코드를 사용한다.

성공 응답은 공통 래퍼를 사용하고, 실제 Response DTO는 `data` 필드에 담아 반환한다.

상세 규칙은 `api-response.md`를 따른다.

```json
{
  "status": 200,
  "code": 2000,
  "message": "요청 성공",
  "data": {
  }
}
```

---

## 6. Swagger Convention

API 문서는 SpringDoc OpenAPI / Swagger를 사용한다.

### 6.1 에러 명세

컨트롤러에서 아래 어노테이션을 사용해 에러 응답 예시를 명세한다.

```java
@ApiErrorCodeExamples({
    ErrorCode.NOT_FOUND_USER,
    ErrorCode.INVALID_REQUEST
})
```

### 작성 원칙

- Controller API에 발생 가능한 ErrorCode를 명시한다.
- ErrorCode 기반으로 Swagger 에러 응답 예시가 자동 생성되도록 한다.
- Controller별로 실제 발생 가능한 에러만 명시한다.
- 임의의 에러 응답 예시를 별도로 만들지 않는다.
