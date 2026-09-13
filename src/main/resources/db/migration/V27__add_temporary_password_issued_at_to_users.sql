-- 임시 비밀번호 발급 시각. NULL이 아니면 현재 비밀번호가 임시 비밀번호라는 뜻이며,
-- 비밀번호를 변경하면 다시 NULL로 돌아간다. 재발급 쿨다운 판정에도 사용한다.
ALTER TABLE users ADD COLUMN IF NOT EXISTS temporary_password_issued_at TIMESTAMPTZ;
