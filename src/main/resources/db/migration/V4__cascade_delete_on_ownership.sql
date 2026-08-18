-- 소유 관계 FK 에 ON DELETE CASCADE 를 건다.
--
-- 지금까지는 워크스페이스/Task/Todo 를 지울 때 하위 데이터를 서비스 코드에서 순서대로
-- 직접 지웠다. 같은 삭제 규칙이 세 벌로 흩어져 있었고, FK 제약 때문에 순서를 사람이
-- 기억해야 했다. 새 엔티티가 추가되면 세 곳을 모두 고쳐야 하고 한 곳이라도 빠뜨리면
-- 제약 위반으로 실패한다.
--
-- 삭제 규칙을 DB 제약으로 내려 자바 코드에서 순서를 신경 쓰지 않도록 한다.
-- 사용자를 가리키는 FK(작성자, 담당자, 업로더 등)는 대상이 아니다.
-- 사용자를 지운다고 그가 쓴 글까지 지워져야 하는 것은 아니기 때문이다.

-- workspace 소속
ALTER TABLE workspace_user DROP FOREIGN KEY fk_workspace_user_workspace;
ALTER TABLE workspace_user ADD CONSTRAINT fk_workspace_user_workspace
    FOREIGN KEY (workspace_id) REFERENCES workspaces (id) ON DELETE CASCADE;

ALTER TABLE workspace_invite DROP FOREIGN KEY fk_workspace_invite_workspace;
ALTER TABLE workspace_invite ADD CONSTRAINT fk_workspace_invite_workspace
    FOREIGN KEY (workspace_id) REFERENCES workspaces (id) ON DELETE CASCADE;

ALTER TABLE task DROP FOREIGN KEY fk_task_workspace;
ALTER TABLE task ADD CONSTRAINT fk_task_workspace
    FOREIGN KEY (workspace_id) REFERENCES workspaces (id) ON DELETE CASCADE;

-- task 소속
ALTER TABLE task_user DROP FOREIGN KEY fk_task_user_task;
ALTER TABLE task_user ADD CONSTRAINT fk_task_user_task
    FOREIGN KEY (task_id) REFERENCES task (id) ON DELETE CASCADE;

ALTER TABLE todos DROP FOREIGN KEY fk_todos_task;
ALTER TABLE todos ADD CONSTRAINT fk_todos_task
    FOREIGN KEY (task_id) REFERENCES task (id) ON DELETE CASCADE;

-- todo 소속
ALTER TABLE todo_work DROP FOREIGN KEY fk_todo_work_todo;
ALTER TABLE todo_work ADD CONSTRAINT fk_todo_work_todo
    FOREIGN KEY (todo_id) REFERENCES todos (id) ON DELETE CASCADE;

ALTER TABLE todo_comments DROP FOREIGN KEY fk_todo_comments_todo;
ALTER TABLE todo_comments ADD CONSTRAINT fk_todo_comments_todo
    FOREIGN KEY (todo_id) REFERENCES todos (id) ON DELETE CASCADE;

-- work 소속
ALTER TABLE todo_files DROP FOREIGN KEY fk_todo_files_work;
ALTER TABLE todo_files ADD CONSTRAINT fk_todo_files_work
    FOREIGN KEY (work_id) REFERENCES todo_work (id) ON DELETE CASCADE;
