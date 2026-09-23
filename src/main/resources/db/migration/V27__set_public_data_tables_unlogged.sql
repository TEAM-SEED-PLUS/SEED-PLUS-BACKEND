-- 반복 갱신되는 공공데이터 테이블의 WAL 발생량을 줄이기 위해 UNLOGGED로 전환한다.
-- DB 비정상 종료 시 데이터가 유실될 수 있다.
ALTER TABLE public.seoul_sdot_foot_traffic SET UNLOGGED;
ALTER TABLE public.commercial_estimated_sales SET UNLOGGED;
