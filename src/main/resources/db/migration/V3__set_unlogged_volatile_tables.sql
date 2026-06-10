-- WAL 아카이브 / EBS 비정상 증가 방지를 위해 휘발성이고 유실돼도 재생성 가능한 테이블을 UNLOGGED로 전환한다.

ALTER TABLE refresh_tokens SET UNLOGGED;