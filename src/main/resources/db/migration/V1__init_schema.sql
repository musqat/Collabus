-- 초기 스키마 베이스라인.
-- 기존 ddl-auto=update 로 생성되던 스키마를 그대로 옮긴 것으로, 이후 변경은 V2, V3... 로 추가한다.

CREATE TABLE users (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    email        VARCHAR(255) NOT NULL,
    nickname     VARCHAR(255) NOT NULL,
    password     VARCHAR(255) NOT NULL,
    tag          VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    role         ENUM('USER','ADMIN') DEFAULT NULL,
    created_at   DATETIME(6)  DEFAULT NULL,
    created_by   VARCHAR(255) DEFAULT NULL,
    updated_at   DATETIME(6)  DEFAULT NULL,
    updated_by   VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    UNIQUE KEY uk_users_display_name (display_name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE workspaces (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    workspace_name VARCHAR(255) NOT NULL,
    description    VARCHAR(255) NOT NULL,
    founder_id     BIGINT       DEFAULT NULL,
    created_at     DATETIME(6)  DEFAULT NULL,
    created_by     VARCHAR(255) DEFAULT NULL,
    updated_at     DATETIME(6)  DEFAULT NULL,
    updated_by     VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_workspaces_founder (founder_id),
    CONSTRAINT fk_workspaces_founder FOREIGN KEY (founder_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE workspace_user (
    user_id      BIGINT       NOT NULL,
    workspace_id BIGINT       NOT NULL,
    role         ENUM('MASTER','MANAGER','MEMBER') NOT NULL,
    created_at   DATETIME(6)  DEFAULT NULL,
    created_by   VARCHAR(255) DEFAULT NULL,
    updated_at   DATETIME(6)  DEFAULT NULL,
    updated_by   VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (user_id, workspace_id),
    KEY idx_workspace_user_workspace (workspace_id),
    CONSTRAINT fk_workspace_user_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_workspace_user_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE workspace_invite (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    workspace_id BIGINT       NOT NULL,
    inviter_id   BIGINT       NOT NULL,
    invitee_id   BIGINT       NOT NULL,
    role         ENUM('MASTER','MANAGER','MEMBER') NOT NULL,
    status       ENUM('PENDING','ACCEPTED','REJECTED') NOT NULL,
    created_at   DATETIME(6)  DEFAULT NULL,
    created_by   VARCHAR(255) DEFAULT NULL,
    updated_at   DATETIME(6)  DEFAULT NULL,
    updated_by   VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_workspace_invite_workspace (workspace_id),
    KEY idx_workspace_invite_inviter (inviter_id),
    KEY idx_workspace_invite_invitee (invitee_id),
    CONSTRAINT fk_workspace_invite_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT fk_workspace_invite_inviter FOREIGN KEY (inviter_id) REFERENCES users (id),
    CONSTRAINT fk_workspace_invite_invitee FOREIGN KEY (invitee_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE task (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    workspace_id    BIGINT       NOT NULL,
    task_manager_id BIGINT       DEFAULT NULL,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    due_date        DATE         DEFAULT NULL,
    created_at      DATETIME(6)  DEFAULT NULL,
    created_by      VARCHAR(255) DEFAULT NULL,
    updated_at      DATETIME(6)  DEFAULT NULL,
    updated_by      VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_task_workspace (workspace_id),
    KEY idx_task_manager (task_manager_id),
    CONSTRAINT fk_task_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT fk_task_manager FOREIGN KEY (task_manager_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE task_user (
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role    ENUM('MANAGER','NORMAL') DEFAULT NULL,
    PRIMARY KEY (task_id, user_id),
    KEY idx_task_user_user (user_id),
    CONSTRAINT fk_task_user_task FOREIGN KEY (task_id) REFERENCES task (id),
    CONSTRAINT fk_task_user_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE todos (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    task_id     BIGINT       NOT NULL,
    assignee_id BIGINT       DEFAULT NULL,
    title       VARCHAR(255) NOT NULL,
    description VARCHAR(255) DEFAULT NULL,
    due_date    DATETIME(6)  NOT NULL,
    done_at     DATETIME(6)  DEFAULT NULL,
    status      ENUM('IN_PROGRESS','WAITING_REVIEW','CONFIRMED') NOT NULL,
    created_at  DATETIME(6)  DEFAULT NULL,
    created_by  VARCHAR(255) DEFAULT NULL,
    updated_at  DATETIME(6)  DEFAULT NULL,
    updated_by  VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_todos_task (task_id),
    KEY idx_todos_assignee (assignee_id),
    CONSTRAINT fk_todos_task FOREIGN KEY (task_id) REFERENCES task (id),
    CONSTRAINT fk_todos_assignee FOREIGN KEY (assignee_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE todo_work (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    todo_id    BIGINT       NOT NULL,
    author_id  BIGINT       NOT NULL,
    title      VARCHAR(255) NOT NULL,
    content    TEXT,
    created_at DATETIME(6)  DEFAULT NULL,
    created_by VARCHAR(255) DEFAULT NULL,
    updated_at DATETIME(6)  DEFAULT NULL,
    updated_by VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_todo_work_todo (todo_id),
    KEY idx_todo_work_author (author_id),
    CONSTRAINT fk_todo_work_todo FOREIGN KEY (todo_id) REFERENCES todos (id),
    CONSTRAINT fk_todo_work_author FOREIGN KEY (author_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE todo_files (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    work_id       BIGINT       NOT NULL,
    uploader_id   BIGINT       NOT NULL,
    file_url      VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    created_at    DATETIME(6)  DEFAULT NULL,
    created_by    VARCHAR(255) DEFAULT NULL,
    updated_at    DATETIME(6)  DEFAULT NULL,
    updated_by    VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_todo_files_work (work_id),
    KEY idx_todo_files_uploader (uploader_id),
    CONSTRAINT fk_todo_files_work FOREIGN KEY (work_id) REFERENCES todo_work (id),
    CONSTRAINT fk_todo_files_uploader FOREIGN KEY (uploader_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE todo_comments (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    todo_id    BIGINT       NOT NULL,
    author_id  BIGINT       NOT NULL,
    content    TEXT,
    created_at DATETIME(6)  DEFAULT NULL,
    created_by VARCHAR(255) DEFAULT NULL,
    updated_at DATETIME(6)  DEFAULT NULL,
    updated_by VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_todo_comments_todo (todo_id),
    KEY idx_todo_comments_author (author_id),
    CONSTRAINT fk_todo_comments_todo FOREIGN KEY (todo_id) REFERENCES todos (id),
    CONSTRAINT fk_todo_comments_author FOREIGN KEY (author_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE notifications (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    user_id           BIGINT       NOT NULL,
    type              ENUM('TASK_ASSIGNED','TODO_COMPLETED','TODO_REVIEW_REQUESTED','WORKSPACE_INVITED','COMMENT_ADDED','TASK_DEADLINE_APPROACHING') NOT NULL,
    message           VARCHAR(255) NOT NULL,
    related_entity_id BIGINT       DEFAULT NULL,
    is_read           BIT(1)       NOT NULL,
    created_at        DATETIME(6)  DEFAULT NULL,
    created_by        VARCHAR(255) DEFAULT NULL,
    updated_at        DATETIME(6)  DEFAULT NULL,
    updated_by        VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_notifications_user (user_id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
