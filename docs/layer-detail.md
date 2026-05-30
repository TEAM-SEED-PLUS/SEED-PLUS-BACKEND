---
description: 계층(global/presentation/application/domain/infrastructure)의 상세 책임과 작성 원칙, DTO/Mapper 컨벤션. 새 클래스 생성 위치 결정, 계층 간 의존성 결정, DTO/Command/Query 객체 설계 시 참조한다.
alwaysApply: false
---

# Layer Convention Detail

`00-core.md`의 의존성 규칙과 계층 책임 요약을 기준으로, 각 계층의 상세 작성 원칙을 정의한다.

---

## 1. global

global 계층은 프로젝트 전반에서 공통으로 사용하는 모듈을 관리한다.

### 주요 역할

- Spring 설정 클래스
- 전역 예외 처리
- 공통 응답 포맷
- 보안/JWT 처리
- 공통 유틸리티
- 상수 및 공통 정책

### 예시 패키지

| Package | Description |
| --- | --- |
| config | Swagger, JPA, CORS, QueryDSL 등 설정 |
| error | 예외 클래스, 에러코드, 예외 핸들러 |
| response | 공통 API 응답 객체 |
| security | JWT, 인증/인가 관련 처리 |
| util | 날짜, 문자열, 랜덤값 등 유틸 |
| common | BaseEntity, 공통 상수, 공통 인터페이스 |

### 작성 원칙

- 특정 도메인에 종속된 로직을 global에 두지 않는다.
- 여러 계층에서 공통으로 필요한 코드만 global에 둔다.
- global이 비즈니스 규칙을 포함하지 않도록 한다.

---

## 2. presentation

presentation 계층은 외부 요청을 받아 application 계층으로 전달하고, 결과를 응답 객체로 변환한다.

### 주요 역할

- Controller
- Request DTO
- Response DTO
- API 문서화 어노테이션
- 파라미터 검증 진입점

### 포함 대상

- REST Controller
- Request DTO
- Response DTO
- Presentation 전용 Mapper

### 작성 원칙

- 비즈니스 로직을 직접 처리하지 않는다.
- application 서비스 호출에 집중한다.
- HTTP 요청/응답 처리에만 집중한다.
- Request DTO validation은 presentation 계층에서 수행한다.
- Controller에서 Entity를 직접 반환하지 않는다.
- Controller에서 Repository를 직접 호출하지 않는다.
- Controller에서 infrastructure 구현체를 직접 호출하지 않는다.

---

## 3. application

application 계층은 시스템의 유스케이스를 구현하는 계층이다.

도메인 객체를 조합하여 실제 서비스 흐름을 수행한다.

### 주요 역할

- 유스케이스 실행
- 트랜잭션 경계 관리
- 도메인 객체 조합
- 포트/interface 정의
- 외부 시스템 연동 흐름 제어

### 포함 대상

- Application Service
- UseCase
- Command 객체
- Query 객체
- Port Interface

### 작성 원칙

- 도메인 규칙은 domain에 위임한다.
- infrastructure 구현체에 직접 의존하지 않는다.
- 외부 기술 구현이 필요한 경우 interface 또는 port를 통해 의존한다.
- 트랜잭션 경계는 application 계층에서 관리한다.
- application 계층에 복잡한 비즈니스 규칙을 직접 작성하지 않는다.
- application 계층은 흐름 제어와 도메인 객체 조합에 집중한다.

---

## 4. domain

domain 계층은 핵심 비즈니스 규칙과 모델을 담는 계층이다.

프로젝트에서 가장 안정적이고 보호되어야 하는 영역이다.

### 주요 역할

- 엔티티
- 값 객체
- 도메인 서비스
- 도메인 정책
- 비즈니스 규칙 검증

### 포함 대상

- Entity
- Repository Interface
- Enum
- Domain Service
- Value Object
- Policy / Rule 객체

### 작성 원칙

- 다른 계층 기술에 의존하지 않는다.
- Spring Web, Security, Infrastructure 구현체에 의존하지 않는다.
- 비즈니스 규칙의 중심이 된다.
- 상태 변경, 계산, 정책, 불변식 검증은 domain에 둔다.
- domain 객체는 스스로 유효한 상태를 유지해야 한다.
- 외부 시스템 호출 로직을 domain에 작성하지 않는다.

---

## 5. infrastructure

infrastructure 계층은 외부 기술과의 실제 연결을 담당한다.

### 주요 역할

- JPA Repository 구현
- QueryDSL 기반 조회 구현
- 외부 API 호출
- 파일 저장소 연동
- 메시징/캐시/외부 클라이언트 구현

### 포함 대상

- Repository Impl
- Query Repository
- External Client
- DB Adapter
- Storage Adapter

### 작성 원칙

- application 또는 domain이 정의한 interface를 구현한다.
- 기술 세부사항을 외부 계층으로 노출하지 않는다.
- DB, 외부 API, 파일 저장소, 캐시, 메시징 등 기술 구현을 담당한다.
- 비즈니스 규칙을 infrastructure에 작성하지 않는다.
- Controller에서 infrastructure 구현체를 직접 호출하지 않도록 한다.

---

## 6. DTO / Mapper Convention

DTO와 Mapper에 대한 상세 규칙은 `dto-mapper.md`를 따른다.

기본 원칙은 다음과 같다.

- Entity를 Controller 응답으로 직접 반환하지 않는다.
- Request DTO와 Response DTO는 presentation 계층에 둔다.
- Request DTO와 Response DTO는 `record`로 작성한다.
- application 계층 내부 전달 객체가 필요한 경우 Command 또는 Query 객체를 사용한다.
- Request DTO는 `toCommand` 또는 `toQuery` 변환 메서드를 제공한다.
- Controller는 Request DTO의 변환 메서드를 호출해 application 계층 입력 객체를 만든다.
- Response DTO는 `from` 정적 팩토리 메서드로 생성한다.
- 단순 변환은 DTO 내부 메서드를 우선하고, 복잡한 변환이 반복될 때만 별도 Mapper 도입을 검토한다.
