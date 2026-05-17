---
description: 목록 조회 API의 페이지네이션 요청/응답 포맷, Spring Pageable 사용 기준, ApiResponse와 페이지 응답 래퍼 구조. 목록/검색/피드/랭킹 API 작성 시 참조한다.
globs: **/*Controller.java, **/*Request.java, **/*Response.java, **/*Query.java, **/*Repository.java
alwaysApply: false
---

# Pagination Convention

목록 조회 API는 페이지네이션을 기본으로 적용한다.

---

## 1. 기본 원칙

- 목록 조회, 검색, 피드, 랭킹 API에는 페이지네이션을 적용한다.
- 성공 응답은 `ApiResponse`로 감싸고, 페이지 결과는 `data` 필드에 담는다.
- Controller는 페이지 요청 값을 application 계층의 Query 객체로 변환한다.
- application 계층은 presentation DTO에 의존하지 않는다.
- Repository 조회에는 Spring Data의 `Pageable` 또는 명시적인 limit/offset 조건을 사용한다.
- 무제한 목록 조회 API를 만들지 않는다.

---

## 2. 요청 파라미터

기본 요청 파라미터는 다음을 사용한다.

| Parameter | Type | Default | Description |
| --- | --- | --- | --- |
| `page` | int | `0` | 0부터 시작하는 페이지 번호 |
| `size` | int | `20` | 한 페이지 크기 |
| `sort` | string | API별 기본값 | 정렬 기준 |

예시:

```text
GET /api/v1/builder-stores?page=0&size=20&sort=createdAt,desc
```

### 작성 원칙

- `page`는 0-base를 사용한다.
- `size` 기본값은 20으로 한다.
- `size` 최대값은 100으로 제한한다.
- 정렬 기본값은 API별로 명시한다.
- 클라이언트에 1-base 페이지가 필요한 경우 프론트엔드에서 변환한다.

---

## 3. 응답 포맷

페이지 응답은 `ApiResponse<PageResponse<T>>` 형태로 반환한다.

```json
{
  "status": 200,
  "code": 2000,
  "message": "요청 성공",
  "data": {
    "content": [],
    "page": 0,
    "size": 20,
    "totalElements": 135,
    "totalPages": 7,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

| Field | Description |
| --- | --- |
| `content` | 현재 페이지 데이터 목록 |
| `page` | 현재 페이지 번호 |
| `size` | 현재 페이지 크기 |
| `totalElements` | 전체 데이터 수 |
| `totalPages` | 전체 페이지 수 |
| `hasNext` | 다음 페이지 존재 여부 |
| `hasPrevious` | 이전 페이지 존재 여부 |

---

## 4. PageResponse

공통 페이지 응답 객체 이름은 `PageResponse`로 한다.

### 작성 원칙

- `PageResponse`는 `global.response` 패키지에 둔다.
- `PageResponse`는 `record`로 작성한다.
- Spring Data `Page<T>`를 받아 생성하는 `from` 정적 팩토리 메서드를 제공한다.
- Slice 기반 조회가 필요한 경우 별도 응답 객체 도입을 검토한다.

### 예시

```java
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
```

---

## 5. Controller 작성 기준

Controller는 요청 파라미터를 검증하고 Query 객체로 변환한다.

```java
@GetMapping
public ResponseEntity<ApiResponse<PageResponse<BuilderStoreResponse>>> search(
        @Valid BuilderStoreSearchRequest request
) {
    Page<BuilderStoreResponse> result = builderStoreQueryService.search(request.toQuery());
    return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
}
```

### 작성 원칙

- Controller에서 직접 Repository를 호출하지 않는다.
- Controller에서 페이지 계산 로직을 작성하지 않는다.
- Request DTO의 `toQuery()`에서 `Pageable` 또는 페이지 조건을 만든다.
- 정렬 허용 필드는 API별로 제한한다.

---

## 6. Query 객체 기준

목록 조회 Query 객체는 검색 조건과 페이지 조건을 함께 가진다.

```java
public record BuilderStoreSearchQuery(
        Long regionId,
        Long industryId,
        Integer minArea,
        Integer maxArea,
        Pageable pageable
) {
}
```

### 작성 원칙

- 조회 API는 `Query` 객체 사용을 우선한다.
- 상태 변경이 없는 목록 조회에는 `Command`를 사용하지 않는다.
- `Pageable`을 application 계층까지 전달할 수 있다.
- QueryDSL을 사용할 경우 `Pageable`의 offset, limit, sort를 반영한다.

---

## 7. 테스트 기준

- 기본 페이지 요청이 적용되는지 검증한다.
- `size` 최대값 초과 시 400 응답을 검증한다.
- 응답에 `content`, `page`, `size`, `totalElements`, `totalPages`, `hasNext`, `hasPrevious`가 포함되는지 검증한다.
- 정렬 파라미터가 허용된 필드만 받는지 검증한다.
- Presentation 테스트는 HTTP 요청/응답 구조에 집중한다.
- Repository 테스트는 offset, limit, sort가 실제 쿼리에 반영되는지 검증한다.
