---
description: SLF4J 기반 운영 로그 작성 기준, 요청 추적 로그, 인증/보안 이벤트 로그, 민감정보 마스킹, 커스텀 예외 처리 원칙.
globs: **/*.java, **/logging/**, **/security/**, **/*Service.java, **/*Controller.java, **/*Exception.java
alwaysApply: false
---

# Logging Convention

본 프로젝트의 로그는 Spring Boot 기본 로깅 체계인 SLF4J/Logback을 사용한다.

---

## 1. 기본 원칙

- 로그는 운영 장애 분석, 보안 이벤트 추적, 성능 확인에 필요한 정보만 남긴다.
- 전화번호, 비밀번호, JWT, Refresh Token, Authorization Header, Cookie 값은 로그에 남기지 않는다.
- 요청 본문 전체를 로그에 남기지 않는다.
- Controller에는 개별 성공 로그를 반복해서 남기지 않고, 공통 요청 로그와 application/service 로그를 우선한다.
- 예외는 `ErrorCode` 기반 `ApplicationException` 또는 `DomainException`으로 변환한다.
- `printStackTrace`, `System.out`, `System.err`, 원시 `RuntimeException` 사용을 금지한다.
- 로그 메시지는 한글로 작성한다.
- 로그 메시지의 시작은 `[클래스명]` 형식으로 작성한다.
- 성공 이벤트는 `[클래스명] 작업 완료 ...` 형식을 따른다.
- 실패 이벤트는 `[클래스명] 작업 실패, 사유=...` 형식을 따른다.

---

## 2. 로그 레벨

| Level | 사용 기준 |
| --- | --- |
| ERROR | 5xx 장애, 예상하지 못한 예외, 즉시 확인이 필요한 서버 오류 |
| WARN | 인증 실패, 접근 거부, 토큰 거절, 비즈니스 검증 실패처럼 운영자가 추적할 가치가 있는 실패 |
| INFO | API 요청 완료, 회원가입/로그인/로그아웃/토큰 재발급 등 주요 정상 이벤트 |
| DEBUG | 로컬 디버깅이나 상세 흐름 확인이 필요한 개발용 정보 |

---

## 3. 요청 추적 로그

모든 `/api/**` 요청은 `ApiRequestLoggingFilter`에서 공통으로 완료 로그를 남긴다.

로그 필드:

- `method`
- `path`
- `query`
- `status`
- `처리시간Ms`
- `requestId`
- `clientIp`
- `사용자ID`
- `userAgent`

요청에 `X-Request-Id` 헤더가 있으면 재사용하고, 없거나 길이가 과하면 서버에서 새 UUID를 발급한다.
응답에도 동일한 `X-Request-Id`를 내려 장애 문의와 서버 로그를 연결할 수 있게 한다.

---

## 4. 인증/보안 이벤트

인증과 보안 이벤트는 민감값 없이 다음과 같이 남긴다.

- 회원가입 성공: `[AuthService] 회원가입 완료 사용자ID={}`
- 로그인 성공: `[AuthService] 로그인 완료 사용자ID={}`
- 로그인 실패: `[AuthService] 로그인 실패, 사유=존재하지 않는 사용자 또는 비밀번호 불일치`
- 토큰 재발급 성공: `[AuthService] 토큰 재발급 완료 사용자ID={}`
- 토큰 재발급 실패: `[AuthService] 토큰 재발급 실패, 사유=리프레시 토큰 만료 사용자ID={}`
- 로그아웃 성공: `[AuthService] 로그아웃 완료 사용자ID={} 리프레시토큰제공여부={}`
- 블랙리스트 토큰 거절: `[JwtAuthenticationFilter] 액세스 토큰 인증 실패, 사유=블랙리스트 토큰`
- 인증 필요 응답: `[JwtAuthenticationEntryPoint] 인증 실패, 사유=인증 정보 없음`
- 접근 거부 응답: `[JwtAccessDeniedHandler] 접근 실패, 사유=권한 부족`

토큰 값, Authorization Header, Cookie 값은 어떤 경우에도 출력하지 않는다.

---

## 5. 예외 처리 로그

예상 가능한 애플리케이션 오류는 `ApplicationException` 또는 `DomainException`으로 던지고,
`GlobalExceptionHandler`에서 `ErrorResponse`로 변환한다.

원칙:

- 직접 `new RuntimeException(...)`, `new IllegalStateException(...)`을 던지지 않는다.
- 외부 라이브러리 예외는 적절한 `ErrorCode`를 가진 커스텀 예외로 감싼다.
- 원인을 보존해야 하는 경우 cause를 받는 생성자를 사용한다.
- 클라이언트에 노출하면 안 되는 내부 사유는 `detail`에 넣지 않는다.

예시:

```java
throw new ApplicationException(ErrorCode.INTERNAL_SERVER_ERROR, cause);
```

```java
throw new ApplicationException(ErrorCode.NOT_FOUND_USER);
```

---

## 6. 환경 설정 접근

애플리케이션 코드에서 `System.getProperty`, `System.getenv`를 직접 호출하지 않는다.

원칙:

- Spring Bean에서는 `@Value`, `@ConfigurationProperties`, `Environment`를 사용한다.
- enum 또는 static 초기화에서 환경변수가 필요하면 해당 값을 읽는 책임을 Spring Bean으로 옮긴다.
- 설정값을 읽지 못한 경우 사용할 기본값은 코드나 `application.yml`에 명시한다.

---

## 7. 설정

로그 레벨은 환경변수로 조정한다.

```yaml
logging:
  level:
    root: ${LOG_LEVEL_ROOT:INFO}
    seed.seedplusbackend: ${LOG_LEVEL_APP:INFO}
    org.springframework.security: ${LOG_LEVEL_SECURITY:WARN}
    org.hibernate.SQL: ${LOG_LEVEL_SQL:WARN}
  pattern:
    level: "%5p [requestId:%X{requestId:-}]"
```

운영 환경에서는 기본적으로 `seed.seedplusbackend=INFO`, `org.springframework.security=WARN`,
`org.hibernate.SQL=WARN`을 사용한다.
