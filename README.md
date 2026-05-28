
# SEED+ Backend

## 프로젝트 소개

SEED+는 상권, 업종, 지역, 점포 데이터를 기반으로 창업 의사결정을 돕는 백엔드 서비스입니다.

주요 기능은 회원 인증, 상권/업종/지역 조회, 점포 및 가상 점포 관리, 생존률/수익률 분석 API 제공입니다.

## 주요 기능

- 회원가입, 로그인, 로그아웃, 토큰 재발급
- JWT 기반 인증/인가
- 상권, 업종, 지역, 건물, 점포 조회
- 가상 점포 생성, 수정, 삭제, 조회
- 가상 점포 좋아요, 댓글, 북마크
- 생존률/수익률 분석 Lambda 연동
- 공통 API 응답 및 전역 예외 처리
- Swagger 기반 API 문서 제공

## 기술 스택

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- QueryDSL
- Flyway
- PostgreSQL
- Gradle
- Docker / Testcontainers
- GitHub Actions
- AWS

## 프로젝트 구조

```text
src/main/java/seed/seedplusbackend
├── auth              # 인증/인가
├── user              # 사용자
├── commercial        # 상권
├── industry          # 업종
├── region            # 지역
├── building          # 건물
├── store             # 점포
├── builderstore      # 가상 점포
├── analysis          # 생존률/수익률 분석
├── metrics           # 분석 지표
├── score             # 점수/랭킹
└── global            # 공통 설정, 보안, 예외, 응답
```

## 로컬 실행 방법

### 1. 환경변수 설정

`.env.example`을 참고하여 로컬 환경변수를 설정합니다.

```bash
cp .env.example .env
```


### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

Windows 환경:

```bash
.\gradlew.bat bootRun
```

## 테스트

```bash
./gradlew test
```

Windows 환경:

```bash
.\gradlew.bat test
```

## API 문서

로컬 실행 후 Swagger UI에서 API 문서를 확인할 수 있습니다.

```text
http://localhost:8080/swagger-ui/index.html
```

## DB 마이그레이션

DB 스키마는 Flyway로 관리합니다.

```text
src/main/resources/db/migration
├── V1__init_seed_core_schema.sql
└── V2__add_phone_number_to_users.sql
```

## 배포

GitHub Actions를 통해 CI/CD를 수행합니다.

- `ci.yml`: 테스트 및 빌드 검증
- `dev-cd.yml`: 개발 환경 배포
- `prod-aws-cd.yml`: 운영 AWS 환경 배포

## 관련 문서

- `docs/00-core.md`
- `docs/api-response.md`
- `docs/error-handling.md`
- `docs/security-rule.md`
- `docs/testing.md`
- `docs/persistence.md`
