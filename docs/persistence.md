---
description: 
globs: **/*Entity.java, **/*Repository.java, **/repository/**, **/domain/**, **/infrastructure/**
alwaysApply: false
---

# Persistence Convention

DB는 PostgreSQL을 사용한다.

ORM은 Spring Data JPA를 사용하고, 동적 쿼리는 QueryDSL을 사용한다.

Spring Boot 3.5.11 기준 Hibernate ORM 6.6.x 계열을 사용하므로 Hibernate deprecated 정책을 고려한다.

---

## 1. ID 전략

ID 생성 전략은 `IDENTITY`를 사용한다.

```java
@GeneratedValue(strategy = GenerationType.IDENTITY)
```

### 작성 원칙

- Entity ID는 IDENTITY 전략을 따른다.
- 다른 ID 생성 전략을 임의로 사용하지 않는다.
- UUID, SEQUENCE, TABLE 전략이 필요한 경우 확인 후 적용한다.

---

## 2. Soft Delete

Soft Delete는 DDL 기준으로 `deletedAt` 컬럼이 존재하는 경우에만 사용한다.

삭제 여부는 `deletedAt IS NULL` 여부로 판단한다.

- `deletedAt == null`: 삭제되지 않은 데이터
- `deletedAt != null`: 삭제된 데이터

### 사용 방식

Hibernate 6.3부터 `@Where`는 deprecated 되었으므로 사용하지 않는다.

Soft Delete 자동 조회 필터링이 필요한 경우 `@SQLRestriction`을 사용한다.

삭제 동작을 물리 삭제가 아닌 `deletedAt` 갱신으로 처리해야 하는 경우 `@SQLDelete`를 사용한다.

```java
@SQLDelete(sql = "UPDATE table_name SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Entity
public class EntityName {
    // ...
}
```

### 작성 원칙

- `@Where`는 사용하지 않는다.
- `@SQLRestriction("deleted_at IS NULL")`을 사용한다.
- `deletedAt` 컬럼이 없는 테이블에 Soft Delete를 임의 적용하지 않는다.
- 삭제 시 물리 삭제하지 않고 `deletedAt`에 삭제 시각을 기록한다.
- `@SQLDelete` 사용 시 SQL의 테이블명, 컬럼명, PK 조건을 실제 DDL과 일치시킨다.
- `@SQLDelete`의 `check = ResultCheckStyle...` 옵션은 사용하지 않는다.
- 삭제 결과 검증이 필요한 경우 `verify = Expectation.RowCount.class` 사용 여부를 검토한다.
- QueryDSL 조회에서도 삭제 데이터를 포함해야 하는 특수 조회가 아니라면 `deletedAt IS NULL` 조건을 유지한다.
- 관리자 조회, 복구, 감사 목적 등 삭제 데이터 포함 조회가 필요한 경우 별도 Repository 메서드 또는 별도 QueryDSL 조회로 명시적으로 처리한다.

### 주의사항

- `@SQLRestriction`은 항상 적용되며 런타임에 비활성화할 수 없다.
- 삭제된 데이터를 조회해야 하는 기능은 `@SQLRestriction`이 붙은 Entity 기본 조회로 처리하지 않는다.
- 삭제 데이터 포함 조회가 필요한 경우 별도 조회 전략을 사용한다.
- Soft Delete 대상 Entity에서 bulk delete를 사용할 때는 `@SQLDelete`가 적용되지 않을 수 있으므로 주의한다.
- `deleteAllInBatch`, JPQL bulk delete, native delete 사용 시 물리 삭제 여부를 반드시 확인한다.

### 확인 필요

- 삭제 데이터 복구 기능 제공 여부
- 관리자 조회에서 삭제 데이터 포함 여부
- Soft Delete 대상 테이블 목록
- `deletedAt` 컬럼명 표준: `deleted_at` 고정 여부
- 삭제자 컬럼 사용 여부
- bulk delete 사용 금지 여부

---

## 3. Repository

### 작성 원칙

- domain의 Repository Interface와 infrastructure의 구현체를 분리한다.
- QueryDSL 기반 조회 구현은 infrastructure에 둔다.
- Repository 구현체에서 비즈니스 규칙을 처리하지 않는다.
- Entity 매핑 규칙은 기존 코드가 있으면 기존 방식을 따른다.

### 확인 필요

- JPA Entity 위치
- domain Entity와 JPA Entity 분리 여부
- Repository Interface 위치
- Query Repository 네이밍
- Auditing 사용 여부
- 연관관계 매핑 원칙
- Fetch Join 사용 기준
