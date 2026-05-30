---
description: 성공 응답 공통 래퍼, data 필드에 Response DTO 반환, 에러 응답과 성공 응답의 책임 분리. Controller 응답 작성 시 참조한다.
alwaysApply: false
---

# API Response Convention

본 프로젝트의 성공 응답은 공통 래퍼를 사용한다.

---

## 1. 기본 원칙

- 성공 응답은 공통 응답 객체로 감싼다.
- 실제 응답 DTO는 `data` 필드에 담아 반환한다.
- Controller는 Entity 또는 application 결과 객체를 직접 반환하지 않는다.
- Controller는 Response DTO를 만든 뒤 공통 응답 래퍼에 담아 반환한다.
- 에러 응답은 `error-handling.md`의 `ErrorResponse` 규칙을 따른다.

---

## 2. 성공 응답 구조

성공 응답은 다음 구조를 기본으로 한다.

```json
{
  "status": 200,
  "code": 2000,
  "message": "요청 성공",
  "data": {
  }
}
```

| Field | Description |
| --- | --- |
| status | HTTP 상태 코드 |
| code | 서비스 커스텀 성공 코드 |
| message | 성공 메시지 |
| data | 실제 응답 DTO |

---

## 3. Controller 작성 원칙

- Controller 반환 타입은 공통 응답 래퍼를 사용한다.
- `data`에는 Response DTO record를 담는다.
- 빈 응답이 필요한 경우 `ResponseEntity<ApiResponse<Void>>`를 사용한다.
- 성공 응답의 HTTP 상태와 커스텀 코드는 일관되게 관리한다.

### 예시

```java
@GetMapping("/me")
public ResponseEntity<ApiResponse<UserMeResponse>> me(
        @AuthenticationPrincipal AuthenticatedUser user
) {
    User result = userService.getMe(user.id());
    return ResponseEntity.ok(ApiResponse.success(UserMeResponse.from(result)));
}
```

---

## 4. 확정 사항

- 공통 성공 응답 객체 이름은 `ApiResponse`로 한다.
- 기본 성공 코드 값은 `2000`으로 한다.
- 기본 성공 메시지는 `"요청 성공"`으로 한다.
- 빈 응답은 `ResponseEntity<ApiResponse<Void>>`로 표현한다.
- 생성 메서드 이름은 `success`로 한다.

## 5. 확인 필요

아래 항목은 실제 공통 응답 객체 구현 시 확정한다.

- 페이징 응답 포맷은 `pagination.md`를 따른다.
