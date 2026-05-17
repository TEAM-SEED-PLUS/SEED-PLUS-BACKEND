---
description: DTO record 사용, Request DTO에서 Command/Query 변환, Response DTO 정적 팩토리 메서드, Controller 변환 책임 기준. Request/Response DTO, Command/Query, Mapper 작성 시 참조한다.
alwaysApply: false
---

# DTO & Mapper Convention

본 프로젝트의 DTO는 Java `record` 타입으로 통일한다.

---

## 1. DTO 기본 원칙

- Request DTO와 Response DTO는 `record`로 작성한다.
- DTO는 presentation 계층에 둔다.
- Entity를 Controller 응답으로 직접 반환하지 않는다.
- Controller는 HTTP 요청/응답 처리와 DTO 변환에 집중한다.
- DTO에는 비즈니스 규칙을 작성하지 않는다.
- JPA Entity와 도메인 객체는 DTO record 전환 대상이 아니다.

---

## 2. Request DTO

Request DTO는 HTTP 요청 값을 표현한다.

### 작성 원칙

- Request DTO는 `record`로 작성한다.
- Bean Validation 어노테이션은 record component에 작성한다.
- Request DTO에서 application 계층 입력 객체로 변환하는 메서드를 제공한다.
- Controller에서는 Request DTO의 변환 메서드를 호출해 Command 또는 Query를 만든다.
- Controller에서 필드를 직접 꺼내 Command 또는 Query를 조립하지 않는다.

### 예시

```java
public record SignupRequest(
        @NotBlank String email,
        @NotBlank String password,
        @NotBlank String name
) {

    public SignupCommand toCommand() {
        return new SignupCommand(email, password, name);
    }
}
```

```java
@PostMapping("/signup")
public ApiResponse<TokenResponse> signup(@Valid @RequestBody SignupRequest request) {
    TokenResult result = authService.signup(request.toCommand());
    return ApiResponse.success(TokenResponse.from(result));
}
```

---

## 3. Response DTO

Response DTO는 HTTP 응답 값을 표현한다.

### 작성 원칙

- Response DTO는 `record`로 작성한다.
- Response DTO 생성은 `from` 정적 팩토리 메서드를 우선 사용한다.
- Entity 또는 application 결과 객체를 그대로 반환하지 않고 Response DTO로 변환한다.
- 단일 객체 변환은 `from`을 사용한다.
- 목록 변환이 필요한 경우 `from` 또는 `fromList` 등 기존 코드와 일관된 이름을 사용한다.

### 예시

```java
public record UserMeResponse(
        Long id,
        String email,
        String name
) {

    public static UserMeResponse from(User user) {
        return new UserMeResponse(user.getId(), user.getEmail(), user.getName());
    }
}
```

---

## 4. Command / Query 사용 기준

Command와 Query는 presentation 계층의 DTO가 application 계층으로 직접 흘러 들어가지 않도록 분리하는 입력 객체다.

### Command

Command는 시스템 상태를 변경하는 유스케이스 입력에 사용한다.

예시:

- 회원가입
- 로그인
- 정보 수정
- 토큰 갱신
- 삭제 요청

```java
public record SignupCommand(
        String email,
        String password,
        String name
) {
}
```

### Query

Query는 시스템 상태를 변경하지 않는 조회 유스케이스 입력에 사용한다.

예시:

- 내 정보 조회
- 목록 조회
- 검색
- 상세 조회

```java
public record UserSearchQuery(
        String keyword,
        int page,
        int size
) {
}
```

### 적용 원칙

- 상태 변경 유스케이스는 Command 사용을 우선한다.
- 조회 유스케이스는 Query 사용을 우선한다.
- 입력 값이 없거나 단순 식별자 하나뿐인 경우에는 별도 객체 생성을 강제하지 않는다.
- 인증 사용자 ID처럼 Controller 밖에서 이미 해석된 값은 Command/Query에 포함할 수 있다.
- application 서비스는 Request DTO에 의존하지 않는다.
- Command/Query는 application 계층에 둔다.

---

## 5. Mapper 사용 기준

- 단순 변환은 DTO 내부의 `toCommand`, `toQuery`, `from` 메서드를 사용한다.
- 변환 로직이 길어지거나 여러 DTO에서 재사용되면 별도 Mapper 도입을 검토한다.
- Mapper를 도입하기 전 기존 방식으로 충분한지 먼저 확인한다.
- MapStruct는 기본으로 사용하지 않는다.

---

## 6. 네이밍

| 대상 | 규칙 | 예시 |
| --- | --- | --- |
| Request DTO | `{UseCase}Request` | `SignupRequest` |
| Response DTO | `{Result}Response` | `TokenResponse`, `UserMeResponse` |
| Command | `{UseCase}Command` | `SignupCommand` |
| Query | `{UseCase}Query` 또는 `{Target}Query` | `UserMeQuery`, `UserSearchQuery` |
| 변환 메서드 | `toCommand`, `toQuery`, `from` | `request.toCommand()`, `UserMeResponse.from(user)` |
