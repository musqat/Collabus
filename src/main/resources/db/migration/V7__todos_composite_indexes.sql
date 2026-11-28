-- Todo 목록 정렬 · 상태 필터 · 진행률 집계용 복합 인덱스. task_id 단일 인덱스는 두 인덱스가 대신함
CREATE INDEX idx_todos_task_due ON todos (task_id, due_date);
CREATE INDEX idx_todos_task_status_due ON todos (task_id, status, due_date);
DROP INDEX idx_todos_task ON todos;
