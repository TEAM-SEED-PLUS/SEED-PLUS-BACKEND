---
description: 네이밍 컨벤션, 코드 스타일(Java 21, Spotless, Lombok, 주석). 새 클래스/메서드 명명, Controller/Service/Repository 등 접미사 결정, 포매팅/Lombok 사용 결정 시 참조한다.
alwaysApply: false
---

# Naming & Code Style

---

## 1. Naming Convention

상세 네이밍 규칙은 아직 확정되지 않았다.

확인 전까지 다음을 지킨다.

- 기존 코드의 네이밍 패턴을 우선한다.
- 새로운 패턴을 임의로 도입하지 않는다.
- Controller, Service, UseCase, Port, Adapter 등의 접미사 규칙은 확인 후 적용한다.

### 확인 필요

- Controller 네이밍
- Application Service 네이밍
- UseCase 네이밍
- Port Interface 네이밍
- Repository Interface 네이밍
- Repository Impl 네이밍
- Query Repository 네이밍
- External Client 네이밍
- DTO 네이밍
- Command / Query 네이밍

---

## 2. Code Style

Java 21과 Spring Boot 3.5.11을 기준으로 작성한다.


### 작성 원칙

- 기존 코드 스타일을 따른다.
- 임의로 import ordering, line length, formatter 규칙을 만들지 않는다.
- 불필요한 주석을 작성하지 않는다.
- 코드로 의도가 드러나지 않는 경우에만 주석을 작성한다.
- Lombok은 프로젝트에서 사용하는 범위 내에서 사용한다.
- Lombok 사용 기준이 불명확한 경우 기존 코드 패턴을 따른다.

### 확인 필요

- Spotless 상세 설정
- Checkstyle 사용 여부
- Sonar 규칙 사용 여부
- import ordering 규칙
- line length 제한
- final 사용 여부
- Lombok 사용 기준
- Builder 사용 기준
- AccessLevel 사용 기준
