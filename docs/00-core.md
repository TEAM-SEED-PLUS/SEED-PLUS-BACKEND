---
description: 
alwaysApply: true
---

# Project Code Convention - Core

## 1. 기본 원칙

이 프로젝트는 **도메인 중심의 계층형 패키지 구조**를 따른다.

코드를 작성하거나 수정할 때는 다음 원칙을 반드시 따른다.

- 아키텍처 규칙을 우선한다.
- 계층 간 책임을 침범하지 않는다.
- 패키지는 domain을 기준으로 먼저 나누고, 각 domain 내부에서 계층을 분리한다.
- 비즈니스 규칙은 domain 계층에 위치시킨다.
- presentation 계층에는 비즈니스 로직을 작성하지 않는다.
- application 계층은 유스케이스 흐름을 조합한다.
- infrastructure 계층은 외부 기술 구현을 담당한다.
- domain 계층은 다른 계층에 의존하지 않는다.
- 확정되지 않은 컨벤션은 임의로 만들지 않는다.
- 기존 코드와 충돌하는 규칙이 있으면 임의 수정하지 말고 확인을 요청한다.

---

## 2. Tech Stack

| Category | Technology | Description |
| --- | --- | --- |
| Language | Java 21 | 백엔드 개발 언어 |
| Framework | Spring Boot 3.5.11 | 애플리케이션 실행 및 설정 관리 |
| Web | Spring Web MVC | REST API 서버 구현 |
| Validation | Spring Validation | 요청 데이터 유효성 검증 |
| ORM | Spring Data JPA | 엔티티 기반 데이터 접근 |
| Query | QueryDSL | 타입 안전한 동적 쿼리 처리 |
| Database | PostgreSQL | 관계형 데이터베이스 |
| API Docs | SpringDoc OpenAPI / Swagger | API 문서 자동 생성 및 테스트 |
| Auth | JWT / JJWT | 토큰 기반 인증 처리 |
| Boilerplate | Lombok | 코드 간결화 |
| Test | Spring Boot Test / JUnit5 | 단위 및 통합 테스트 |
| Assertion | AssertJ | 테스트 검증 |
| Mocking | Mockito | 테스트 대역 구성 |
| Build Tool | Gradle | 빌드 및 의존성 관리 |
| Formatter | Spotless | 빌드 과정에서 코드 포맷팅 적용 |

---

## 3. Architecture Overview

본 프로젝트는 **도메인 중심의 계층형 패키지 구조**를 기반으로 구성한다.

패키지는 먼저 도메인을 기준으로 분리하고, 각 도메인 내부를 역할과 책임에 따라 다음 계층으로 분리한다.

- presentation
- application
- domain
- infrastructure

전역 공통 모듈은 global 패키지에서 관리한다.

| Layer | Role |
| --- | --- |
| presentation | 외부 요청/응답 처리, API 진입점 |
| application | 유스케이스 실행, 서비스 흐름 제어 |
| domain | 핵심 비즈니스 규칙과 도메인 모델 |
| infrastructure | DB, 외부 API, 파일 저장소 등 외부 기술 구현 |
| global | 전역 공통 설정 및 예외 처리, 보안, 유틸 |

---

## 4. Package Structure

패키지 구조는 **domain 기준 분리**를 따른다.

base package는 `seed.seedplusbackend`를 사용한다.

```text
seed.seedplusbackend
├── global
│   ├── config
│   ├── error
│   ├── response
│   ├── security
│   ├── util
│   └── common
│
├── <domain-name>
│   ├── presentation
│   ├── application
│   ├── domain
│   └── infrastructure
│
├── <domain-name>
│   ├── presentation
│   ├── application
│   ├── domain
│   └── infrastructure
```

예시:

```text
seed.seedplusbackend
├── global
│
├── user
│   ├── presentation
│   ├── application
│   ├── domain
│   └── infrastructure
│
├── order
│   ├── presentation
│   ├── application
│   ├── domain
│   └── infrastructure
```

### 패키지 원칙

- 도메인별 기능은 각 도메인 패키지 내부에 배치한다.
- 특정 도메인에 종속된 코드는 global에 두지 않는다.
- 여러 도메인에서 공통으로 사용하는 설정, 응답, 예외, 보안, 유틸만 global에 둔다.
- 도메인 내부에서도 계층 간 의존성 규칙을 반드시 지킨다.

---

## 5. Dependency Rule

의존성 방향은 다음 규칙을 따른다.

```text
presentation → application → domain
infrastructure → domain, application
global → 전체 공통 지원
```

### 의존성 원칙

- presentation은 application에만 의존한다.
- presentation은 domain, infrastructure에 직접 의존하지 않는다.
- application은 유스케이스를 조합하고 domain을 사용한다.
- application은 infrastructure 구현체에 직접 의존하지 않는다.
- application은 필요한 경우 interface 또는 port를 정의하고 이를 통해 외부 구현과 연결한다.
- domain은 다른 계층에 의존하지 않는다.
- domain에는 Spring Web, Security, Infrastructure 구현 의존성을 두지 않는다.
- infrastructure는 외부 기술 구현체를 제공한다.
- infrastructure는 domain 또는 application이 정의한 interface를 구현한다.
- global은 전역적으로 필요한 공통 모듈을 제공한다.

---

## 6. Layer Responsibility Summary

각 계층의 상세 책임/포함 대상/작성 원칙은 `layer-detail.md` 참조.

| Layer | 핵심 책임 | 금지 사항 |
| --- | --- | --- |
| presentation | HTTP 진입점, Request/Response DTO, Validation | 비즈니스 로직 작성, Entity 직접 반환, Repository/Infrastructure 직접 호출 |
| application | 유스케이스 흐름 조합, 트랜잭션 경계 관리 | 복잡한 도메인 규칙 직접 작성, infrastructure 구현체 직접 의존 |
| domain | 비즈니스 규칙, 상태 전이, 불변식 검증 | 다른 계층 기술 의존(Spring Web/Security/Infrastructure) |
| infrastructure | DB/외부 API/파일 저장소 등 외부 기술 구현 | 비즈니스 규칙 작성 |
| global | 공통 설정/예외/응답/보안/유틸 | 특정 도메인 종속 로직 포함 |

---

## 7. Security Convention

보안 규칙은 `security.md`를 따른다.

---

## 8. 작업 규칙

코드를 생성하거나 수정할 때는 다음을 따른다.

- 현재 아키텍처 규칙을 위반하는 코드를 생성하지 않는다.
- 도메인 기준 패키지 구조를 우선한다.
- Controller에 비즈니스 로직을 넣지 않는다.
- Application Service에 복잡한 도메인 규칙을 넣지 않는다.
- Domain 계층에 infrastructure 의존성을 넣지 않는다.
- Infrastructure 구현체를 presentation에서 직접 호출하지 않는다.
- `throw new RuntimeException()`을 사용하지 않는다.
- 예외는 반드시 ErrorCode 기반 ApplicationException 또는 DomainException으로 처리한다.
- 에러 응답은 ErrorResponse 포맷을 따른다.
- 성공 응답은 `ApiResponse` 공통 래퍼를 사용하고 실제 Response DTO는 `data` 필드에 담는다.
- 기본 성공 메시지는 `"요청 성공"`으로 한다.
- 빈 응답은 `ResponseEntity<ApiResponse<Void>>`로 표현한다.
- Request/Response DTO는 `record`로 작성한다.
- Request DTO는 `toCommand` 또는 `toQuery` 변환 메서드를 제공한다.
- Response DTO는 `from` 정적 팩토리 메서드로 생성한다.
- Swagger 에러 예시는 `@ApiErrorCodeExamples`를 사용한다.
- Entity ID는 IDENTITY 전략을 따른다.
- Soft Delete는 DDL 기준 `deletedAt` 컬럼이 존재할 때만 적용한다.
- Soft Delete가 필요한 Entity는 `@Where`를 사용하지 않고 `@SQLRestriction`을 사용한다.
- 확정되지 않은 컨벤션은 임의로 정하지 않는다.
- 누락된 정책이 필요한 경우 코드 생성 전에 확인이 필요하다고 명시한다.
- 기존 코드와 규칙이 충돌하면 기존 코드를 무조건 변경하지 말고 충돌 내용을 설명한다.
- 테스트 작성 시 Domain 테스트를 가장 우선한다.
- 테스트에는 한글 `@DisplayName`을 명시한다.
- fixture를 사용할 경우 목적과 상태가 명확해야 한다.
- Presentation 테스트는 HTTP 계약 검증에 집중한다.
- Application 테스트는 흐름 검증에 집중한다.
- Infrastructure 테스트는 실제 기술 구현 검증에 집중한다.

---

## 9. 작업 맥락별 참조 규칙

작업 맥락에 따라 아래 규칙 파일을 참조한다.

| 작업 맥락 | 참조 파일 |
| --- | --- |
| 계층 상세 책임, DTO/Command/Query/Mapper 결정, 새 클래스 배치 | `layer-detail.md` |
| Request/Response DTO record, Command/Query 변환, `from` 정적 팩토리 | `dto-mapper.md` |
| 성공 응답 공통 래퍼, `data` 필드 응답 구조 | `api-response.md` |
| ErrorCode, ApplicationException, DomainException, GlobalExceptionHandler, Swagger 에러 명세 | `error-handling.md` |
| Spring Security, JWT, Refresh Token Cookie, CSRF, 인증 사용자 처리 | `security.md` |
| Entity, Repository, Soft Delete(`@SQLRestriction`/`@SQLDelete`), JPA/QueryDSL | `persistence.md` |
| 테스트 클래스 작성(Domain/Presentation/Application/Infrastructure), Fixture, Test Naming | `testing.md` |
| 네이밍 결정, Spotless/Lombok/주석 등 코드 스타일 | `naming-style.md` |
