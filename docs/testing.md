---
description: 테스트 우선순위(Domain > Presentation > Infrastructure > Application > 통합), 계층별 테스트 작성 원칙, Test Naming, Fixture 사용 기준. 테스트 클래스 작성 시 참조한다.
globs: **/*Test.java, **/*IntegrationTest.java, **/test/**, **/*Tests.java
alwaysApply: false
---

# Test Convention

본 프로젝트의 테스트는 도메인 규칙의 신뢰성 확보를 최우선으로 한다.

사용 기술은 다음을 따른다.

- JUnit5
- AssertJ
- Mockito
- Spring Boot Test

테스트 우선순위는 다음과 같다.

1. Domain Test
2. Presentation MVC Test
3. 핵심 Infrastructure Test
4. 필수 Application Test
5. 선택적 통합 테스트

---

## 1. 공통 테스트 원칙

- 테스트는 문서화 목적을 포함한다.
- `@DisplayName`을 명시한다.
- `@DisplayName`은 한글로 작성한다.
- DisplayName은 테스트 대상 정책 또는 시나리오가 드러나도록 작성한다.
- 테스트 메서드명은 행위와 기대 결과가 드러나도록 작성한다.
- Given / When / Then 구조를 유지한다.
- 하나의 테스트는 하나의 규칙 또는 하나의 시나리오를 검증한다.
- fixture를 사용할 경우 목적과 범위가 명확해야 한다.
- fixture가 테스트 의도를 흐리면 테스트 내부에서 직접 생성한다.
- 불필요한 fixture 남용을 피한다.

---

## 2. Domain Test

Domain 테스트는 프로젝트의 핵심 테스트 계층이다.

### 테스트 대상

- Entity
- Value Object
- Enum 기반 상태 전이
- Domain Service
- Policy / Rule 객체
- 계산 로직
- 도메인 예외 발생 조건

### 검증 대상

- 상태 전이가 올바르게 일어나는지
- 허용되지 않은 상태 변경이 차단되는지
- 계산 결과가 기대값과 일치하는지
- 비즈니스 불변식이 유지되는지
- 잘못된 입력/상태에서 예외가 발생하는지

### 작성 원칙

- Spring Context 없이 순수 단위 테스트로 작성한다.
- Given / When / Then 구조를 유지한다.
- 테스트 이름은 행동과 기대 결과 중심으로 작성한다.
- 하나의 테스트는 하나의 규칙만 검증한다.
- Application, Repository mocking 없이 도메인 객체만으로 검증한다.
- AssertJ를 사용해 검증한다.

---

## 3. Presentation Test

Presentation 테스트는 Controller가 HTTP 계층의 책임을 올바르게 수행하는지 검증한다.

### 테스트 대상

- Controller
- Request DTO Validation
- Query Parameter Binding
- Path Variable Binding
- 공통 예외 응답 형식
- HTTP Status Code
- 응답 JSON 구조

### 작성 원칙

- Spring MVC 테스트를 우선 사용한다.
- Application Service는 Mockito 기반 mock 처리한다.
- Controller에서 비즈니스 로직을 직접 검증하지 않는다.
- 요청 검증, 상태 코드, 응답 구조에 집중한다.
- ErrorResponse 포맷이 유지되는지 검증한다.
- Swagger 어노테이션 자체보다는 API 계약과 응답 구조를 검증한다.

### 중점 테스트 항목

- 필수값 누락 시 400 응답
- 잘못된 enum 값 요청 처리
- 잘못된 숫자/날짜 포맷 처리
- 공통 예외 응답 스펙 유지 여부
- 커스텀 에러 코드 반환 여부

---

## 4. Application Test

Application 테스트는 유스케이스가 올바른 순서로 협력 객체를 호출하고 흐름을 조합하는지 검증한다.

### 테스트 대상

- Application Service
- UseCase
- Command Handler
- Query Service

### 작성 원칙

- Domain 테스트 이후 필요한 흐름 위주로 작성한다.
- Repository, Port, External Client는 Mockito 기반 mock 처리한다.
- 비즈니스 규칙을 다시 상세 검증하지 않는다.
- 계산 결과보다 호출 순서와 흐름을 검증한다.
- Application 테스트가 과도하게 많아지면 도메인 규칙이 application으로 새고 있는지 의심한다.

---

## 5. Infrastructure Test

Infrastructure 테스트는 기술 구현이 실제 환경과 맞게 동작하는지 확인한다.

### 테스트 대상

- JPA Repository
- QueryDSL Repository
- Entity Mapping
- DB 제약조건 반영 여부
- 외부 API Client
- 파일 저장소 Adapter
- JWT 관련 실제 직렬화/역직렬화 구현

### 작성 원칙

- JPA/QueryDSL은 슬라이스 테스트 또는 통합 테스트로 검증한다.
- Repository 테스트는 실제 DB와 유사한 환경에서 수행하는 것이 좋다.
- 외부 API는 실제 호출보다 mock server 또는 stub 기반 테스트를 우선한다.
- mock만으로 끝내지 않고 기술 구현이 실제로 동작하는지 검증한다.

### 확인 필요

- Testcontainers 사용 여부
- 테스트 DB 기준
- Repository 테스트에서 H2 사용 여부
- PostgreSQL 기반 테스트 강제 여부

---

## 6. Test Naming

### 테스트 클래스명

```text
{TargetClassName}Test
{TargetClassName}IntegrationTest
```

### 테스트 메서드명

행위와 기대 결과가 드러나도록 작성한다.

예시:

```text
calculateScore_returnsHighGrade_whenRevenueAndTrafficAreHigh
changeStatus_throwsException_whenTransitionIsNotAllowed
createBuilderRequest_returnsBadRequest_whenRegionIsNull
```

### DisplayName

DisplayName은 한글로 작성한다.

정책 또는 시나리오가 드러나도록 작성한다.

예시:

```text
"필수값이 누락되면 400 Bad Request를 반환한다"
"허용되지 않은 상태 전이는 예외를 발생시킨다"
"존재하지 않는 사용자를 조회하면 커스텀 에러 코드를 반환한다"
```

---

## 7. Fixture Convention

fixture는 사용할 수 있다.

단, fixture 사용 목적이 명확해야 한다.

### 작성 원칙

- fixture는 테스트 가독성을 높이는 경우에만 사용한다.
- fixture가 테스트의 핵심 조건을 숨기면 사용하지 않는다.
- 테스트마다 중요한 값은 테스트 코드에서 명시적으로 드러나야 한다.
- 여러 테스트에서 반복되는 객체 생성만 fixture로 분리한다.
- fixture 이름은 어떤 상태의 객체인지 드러나야 한다.

### 예시

```java
User activeUser = UserFixture.activeUser();
```

```java
Order paidOrder = OrderFixture.paidOrder();
```
