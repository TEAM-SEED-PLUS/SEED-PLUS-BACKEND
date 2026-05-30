---
description: Spring Security, JWT 인증, Access Token/Refresh Token 처리, CSRF 적용 범위, 보안 예외 응답, 인증 사용자 사용 기준. 보안 관련 코드 작성 시 참조한다.
globs: **/security/**, **/auth/**, **/*Controller.java
alwaysApply: false
---

# Security Convention

본 프로젝트의 인증/인가는 Spring Security와 JWT를 기반으로 처리한다.

---

## 1. 기본 구조

| 구성 요소 | 역할 |
| --- | --- |
| `SecurityConfig` | SecurityFilterChain, CSRF, 인가 경로, 예외 핸들러 설정 |
| `JwtAuthenticationFilter` | Access Token 검증 및 SecurityContext 인증 저장 |
| `JwtTokenProvider` | JWT 생성, 파싱, 인증 객체 변환 |
| `AuthenticatedUser` | 인증된 사용자 principal |
| `JwtAuthenticationEntryPoint` | 미인증 요청 처리 |
| `JwtAccessDeniedHandler` | 권한 부족 요청 처리 |
| `SecurityErrorResponseWriter` | 보안 계층의 에러 응답 작성 |
| `RefreshTokenCookieManager` | Refresh Token Cookie 생성/삭제 |

---

## 2. 인증 방식

- Access Token은 `Authorization: Bearer {token}` 헤더로 전달한다.
- Refresh Token은 `refreshToken` 이름의 HttpOnly Cookie로 전달한다.
- 인증 성공 시 `AuthenticatedUser`를 principal로 사용한다.
- Controller에서는 `@AuthenticationPrincipal AuthenticatedUser`로 인증 사용자를 받는다.
- Controller에서 JWT를 직접 파싱하지 않는다.
- JWT 파싱, 검증, Authentication 생성은 `JwtTokenProvider`와 `JwtAuthenticationFilter`가 담당한다.

---

## 3. JWT Token 규칙

Access Token과 Refresh Token은 `tokenType` claim으로 구분한다.

| Token | tokenType | 용도 |
| --- | --- | --- |
| Access Token | `access` | API 인증 |
| Refresh Token | `refresh` | 토큰 재발급 |

### 작성 원칙

- Access Token과 Refresh Token을 서로 대체해서 사용하지 않는다.
- 토큰 타입이 맞지 않으면 `INVALID_TOKEN`으로 처리한다.
- 만료된 Access Token은 `EXPIRED_TOKEN`으로 처리한다.
- 만료된 Refresh Token은 `EXPIRED_REFRESH_TOKEN`으로 처리한다.
- JWT subject에는 사용자 ID를 저장한다.
- 사용자 권한은 `ROLE_{role}` 형식으로 사용한다.
- Access Token의 jti는 로그아웃 이후 블랙리스트 검증에 사용한다.

---

## 4. Security Filter 규칙

`JwtAuthenticationFilter`는 `OncePerRequestFilter`로 동작한다.

### Public Auth Path

다음 인증 API는 JWT 필터를 적용하지 않는다.

- `/api/v1/auth/csrf`
- `/api/v1/auth/signup`
- `/api/v1/auth/login`
- `/api/v1/auth/reissue`

### 작성 원칙

- Public path는 `SecurityConfig`의 permitAll 설정과 `JwtAuthenticationFilter`의 제외 경로를 함께 확인한다.
- Access Token이 없는 요청은 SecurityContext를 설정하지 않고 다음 필터로 전달한다.
- Access Token이 있으면 토큰을 검증하고 `SecurityContextHolder`에 Authentication을 저장한다.
- 토큰 검증 실패 시 SecurityContext를 비우고 `ErrorResponse` 형식으로 응답한다.
- 블랙리스트에 등록된 Access Token jti는 `INVALID_TOKEN`으로 처리한다.

---

## 5. CSRF 규칙

본 프로젝트는 stateless JWT 인증을 사용하지만 Refresh Token이 Cookie로 전달되므로 재발급 API에 CSRF 보호를 적용한다.

### 적용 범위

- `POST /api/v1/auth/reissue`

### 작성 원칙

- CSRF Token은 `/api/v1/auth/csrf`에서 발급한다.
- CSRF Cookie는 `SameSite=None`, `Secure=true`, `Path=/` 설정을 사용한다.
- Refresh Token Cookie는 `HttpOnly=true`, `SameSite=None`, `Secure=true`, `Path=/api/v1/auth` 설정을 사용한다.
- Cookie 기반 인증 범위를 늘릴 때는 CSRF 보호 대상도 함께 검토한다.

---

## 6. 인가 규칙

### Permit All

다음 요청은 인증 없이 접근할 수 있다.

- `OPTIONS /**`
- `/api/v1/auth/csrf`
- `/api/v1/auth/signup`
- `/api/v1/auth/login`
- `/api/v1/auth/reissue`
- `/swagger-ui/**`
- `/swagger-ui.html`
- `/v3/api-docs/**`

그 외 요청은 인증이 필요하다.

### 작성 원칙

- 인증이 필요한 API는 기본적으로 `authenticated()` 보호를 받는다.
- 메서드 단위 권한 제어가 필요한 경우 `@EnableMethodSecurity` 기반 어노테이션 사용을 검토한다.
- 권한 부족은 `FORBIDDEN`으로 처리한다.
- 미인증은 `UNAUTHORIZED`로 처리한다.

---

## 7. 보안 예외 응답

보안 필터와 Spring Security 예외 핸들러에서는 `SecurityErrorResponseWriter`를 통해 응답한다.

### 작성 원칙

- 보안 계층에서도 `ErrorResponse` 포맷을 유지한다.
- 미인증 요청은 `JwtAuthenticationEntryPoint`에서 처리한다.
- 권한 부족 요청은 `JwtAccessDeniedHandler`에서 처리한다.
- Controller에서 보안 에러 응답을 직접 조립하지 않는다.
- 토큰 검증 실패는 `ApplicationException`과 `ErrorCode` 기반으로 처리한다.

---

## 8. Logout 규칙

- 로그아웃 시 Access Token jti를 블랙리스트에 등록한다.
- 로그아웃 시 Refresh Token Cookie를 삭제한다.
- Refresh Token이 존재하면 저장소에서도 무효화한다.
- 로그아웃 API는 인증된 사용자만 호출할 수 있다.

---

## 9. 테스트 기준

- JWT 생성/파싱은 실제 직렬화/역직렬화 동작을 검증한다.
- 인증 필터는 유효 토큰, 만료 토큰, 잘못된 토큰, 블랙리스트 토큰을 검증한다.
- Controller 테스트는 인증 필요 여부와 HTTP 응답 계약에 집중한다.
- 보안 에러 응답은 `ErrorResponse` 포맷을 유지하는지 검증한다.
