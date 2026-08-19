-- 탈퇴한 계정을 지우지 않고 표시만 바꾼다.
-- users 를 참조하는 FK 열한 개가 NO ACTION 이라 물리 삭제는 실패한다.
-- 남긴 댓글과 Task 는 그대로 두고 작성자만 익명으로 보이게 한다.

ALTER TABLE users
    ADD COLUMN deleted_at DATETIME(6) NULL;

-- 탈퇴 계정을 걸러내는 조회가 잦다
CREATE INDEX idx_users_deleted_at ON users (deleted_at);
