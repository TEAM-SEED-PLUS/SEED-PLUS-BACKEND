-- =========================================================================
-- JPA 개발 모드 보완 스크립트 (1) : 부팅 "전"에 1회 실행
-- =========================================================================
-- FLYWAY_ENABLED=false 로 Hibernate ddl-auto 가 스키마를 만들 때,
-- Hibernate 가 만들지 못하는 것들을 채워주는 스크립트다.
--
-- 이 파일은 Flyway 마이그레이션이 아니다. 파일명에 V/R 접두사를 붙이면
-- Flyway 가 실제 마이그레이션으로 인식해 실행하니 이름을 바꾸지 말 것.
--
--   psql "$DB_URL" -f src/main/resources/db/migration/manual/jpa-01-before-boot.sql
--
-- PostGIS 확장은 buildings.location GEOGRAPHY(POINT, 4326) 컬럼보다 먼저 있어야 해서
-- 앱을 띄우기 전에 실행한다. 이미 설치돼 있으면 그냥 통과한다.
-- =========================================================================

CREATE EXTENSION IF NOT EXISTS postgis;
