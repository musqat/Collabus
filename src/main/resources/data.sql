-- Admin 계정 생성
INSERT IGNORE INTO users (email, nickname, password, tag, display_name, role, created_at, updated_at) VALUES
('admin@collabus.com', 'admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '0000', 'admin#0000', 'ADMIN', NOW(), NOW());

-- 더미 사용자 10명 생성 (비밀번호: password)
INSERT IGNORE INTO users (email, nickname, password, tag, display_name, role, created_at, updated_at) VALUES
('user1@test.com', 'user1', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '0001', 'user1#0001', 'USER', NOW(), NOW()),
('user2@test.com', 'user2', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '0002', 'user2#0002', 'USER', NOW(), NOW()),
('user3@test.com', 'user3', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '0003', 'user3#0003', 'USER', NOW(), NOW()),
('user4@test.com', 'user4', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '0004', 'user4#0004', 'USER', NOW(), NOW()),
('user5@test.com', 'user5', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '0005', 'user5#0005', 'USER', NOW(), NOW()),
('user6@test.com', 'user6', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '0006', 'user6#0006', 'USER', NOW(), NOW()),
('user7@test.com', 'user7', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '0007', 'user7#0007', 'USER', NOW(), NOW()),
('user8@test.com', 'user8', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '0008', 'user8#0008', 'USER', NOW(), NOW()),
('user9@test.com', 'user9', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '0009', 'user9#0009', 'USER', NOW(), NOW()),
('user10@test.com', 'user10', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '0010', 'user10#0010', 'USER', NOW(), NOW());

-- Workspace 3개 생성
INSERT IGNORE INTO workspaces (workspace_name, description, founder_id, created_at, updated_at) VALUES
('마케팅팀', '회사의 마케팅 업무를 담당하는 팀입니다. 브랜드 관리, 광고 캠페인, SNS 마케팅 등을 수행합니다.', 2, NOW(), NOW()),
('개발팀', '소프트웨어 개발 및 유지보수를 담당하는 팀입니다. 프론트엔드, 백엔드, 인프라 관리를 수행합니다.', 5, NOW(), NOW()),
('디자인팀', 'UI/UX 디자인과 브랜딩을 담당하는 팀입니다. 사용자 경험 개선과 시각 디자인을 수행합니다.', 8, NOW(), NOW());

-- Workspace 멤버 추가
-- 마케팅팀 (workspace_id=1)
INSERT IGNORE INTO workspace_user (workspace_id, user_id, role, created_at, updated_at) VALUES
(1, 1, 'MASTER', NOW(), NOW()),
(1, 2, 'MASTER', NOW(), NOW()),
(1, 3, 'MANAGER', NOW(), NOW()),
(1, 4, 'MEMBER', NOW(), NOW());

-- 개발팀 (workspace_id=2)
INSERT IGNORE INTO workspace_user (workspace_id, user_id, role, created_at, updated_at) VALUES
(2, 1, 'MASTER', NOW(), NOW()),
(2, 5, 'MASTER', NOW(), NOW()),
(2, 6, 'MANAGER', NOW(), NOW()),
(2, 7, 'MEMBER', NOW(), NOW()),
(2, 8, 'MEMBER', NOW(), NOW());

-- 디자인팀 (workspace_id=3)
INSERT IGNORE INTO workspace_user (workspace_id, user_id, role, created_at, updated_at) VALUES
(3, 1, 'MASTER', NOW(), NOW()),
(3, 8, 'MASTER', NOW(), NOW()),
(3, 9, 'MANAGER', NOW(), NOW()),
(3, 10, 'MEMBER', NOW(), NOW());

-- Task 생성 (각 Workspace에 3개씩, 총 9개)
-- 마케팅팀 Tasks
INSERT IGNORE INTO task (workspace_id, title, description, task_manager_id, due_date, created_at, updated_at) VALUES
(1, '2025 Q1 마케팅 캠페인', '2025년 1분기 마케팅 캠페인 기획 및 실행', 2, DATE_ADD(CURDATE(), INTERVAL 30 DAY), NOW(), NOW()),
(1, 'SNS 콘텐츠 제작', '인스타그램, 페이스북, 유튜브 콘텐츠 제작 및 관리', 2, DATE_ADD(CURDATE(), INTERVAL 60 DAY), NOW(), NOW()),
(1, '브랜드 리뉴얼', '회사 브랜드 아이덴티티 재정립 및 리뉴얼 프로젝트', 1, DATE_ADD(CURDATE(), INTERVAL 90 DAY), NOW(), NOW());

-- 개발팀 Tasks
INSERT IGNORE INTO task (workspace_id, title, description, task_manager_id, due_date, created_at, updated_at) VALUES
(2, '백엔드 API 개발', 'RESTful API 설계 및 구현', 5, DATE_ADD(CURDATE(), INTERVAL 45 DAY), NOW(), NOW()),
(2, '프론트엔드 개발', 'React 기반 사용자 인터페이스 개발', 5, DATE_ADD(CURDATE(), INTERVAL 45 DAY), NOW(), NOW()),
(2, '데이터베이스 최적화', '쿼리 성능 개선 및 인덱스 최적화', 4, DATE_ADD(CURDATE(), INTERVAL 30 DAY), NOW(), NOW());

-- 디자인팀 Tasks
INSERT IGNORE INTO task (workspace_id, title, description, task_manager_id, due_date, created_at, updated_at) VALUES
(3, '모바일 앱 UI 디자인', 'iOS/Android 앱 UI/UX 디자인', 8, DATE_ADD(CURDATE(), INTERVAL 60 DAY), NOW(), NOW()),
(3, '웹사이트 리디자인', '회사 공식 웹사이트 리디자인', 8, DATE_ADD(CURDATE(), INTERVAL 75 DAY), NOW(), NOW()),
(3, '디자인 시스템 구축', '일관된 디자인을 위한 컴포넌트 라이브러리 구축', 7, DATE_ADD(CURDATE(), INTERVAL 90 DAY), NOW(), NOW());

-- Task 멤버 추가
-- 마케팅팀 Tasks (1,2,3)
INSERT IGNORE INTO task_user (task_id, user_id, role) VALUES
(1, 2, 'MANAGER'), (1, 1, 'NORMAL'), (1, 3, 'NORMAL'),
(2, 2, 'MANAGER'), (2, 1, 'NORMAL'), (2, 3, 'NORMAL'),
(3, 1, 'MANAGER'), (3, 2, 'NORMAL'), (3, 3, 'NORMAL');

-- 개발팀 Tasks (4,5,6)
INSERT IGNORE INTO task_user (task_id, user_id, role) VALUES
(4, 5, 'MANAGER'), (4, 4, 'NORMAL'), (4, 6, 'NORMAL'), (4, 7, 'NORMAL'),
(5, 5, 'MANAGER'), (5, 4, 'NORMAL'), (5, 6, 'NORMAL'), (5, 7, 'NORMAL'),
(6, 4, 'MANAGER'), (6, 5, 'NORMAL'), (6, 6, 'NORMAL'), (6, 7, 'NORMAL');

-- 디자인팀 Tasks (7,8,9)
INSERT IGNORE INTO task_user (task_id, user_id, role) VALUES
(7, 8, 'MANAGER'), (7, 7, 'NORMAL'), (7, 9, 'NORMAL'), (7, 10, 'NORMAL'),
(8, 8, 'MANAGER'), (8, 7, 'NORMAL'), (8, 9, 'NORMAL'), (8, 10, 'NORMAL'),
(9, 7, 'MANAGER'), (9, 8, 'NORMAL'), (9, 9, 'NORMAL'), (9, 10, 'NORMAL');

-- Todos 생성 (각 Task마다 10개씩, 총 90개)
-- Task 1의 Todos
INSERT IGNORE INTO todos (task_id, assignee_id, title, description, due_date, status, created_at, updated_at) VALUES
(1, 1, '구현 - 2025 기능 1', '2025 Q1 마케팅 캠페인의 세부 작업입니다. 해당 업무를 완료하고 결과물을 작업 내용에 등록해주세요.', DATE_ADD(NOW(), INTERVAL 5 DAY), 'CONFIRMED', NOW(), NOW()),
(1, 2, '설계 - 2025 모듈 2', '2025 Q1 마케팅 캠페인의 세부 작업입니다. 해당 업무를 완료하고 결과물을 작업 내용에 등록해주세요.', DATE_ADD(NOW(), INTERVAL 10 DAY), 'IN_PROGRESS', NOW(), NOW()),
(1, 3, '테스트 - 2025 컴포넌트 3', '2025 Q1 마케팅 캠페인의 세부 작업입니다. 해당 업무를 완료하고 결과물을 작업 내용에 등록해주세요.', DATE_ADD(NOW(), INTERVAL 15 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(1, 1, '검토 - 2025 API 4', '2025 Q1 마케팅 캠페인의 세부 작업입니다. 해당 업무를 완료하고 결과물을 작업 내용에 등록해주세요.', DATE_ADD(NOW(), INTERVAL 20 DAY), 'CONFIRMED', NOW(), NOW()),
(1, 2, '분석 - 2025 UI 5', '2025 Q1 마케팅 캠페인의 세부 작업입니다. 해당 업무를 완료하고 결과물을 작업 내용에 등록해주세요.', DATE_ADD(NOW(), INTERVAL 8 DAY), 'IN_PROGRESS', NOW(), NOW()),
(1, 3, '작성 - 2025 데이터 6', '2025 Q1 마케팅 캠페인의 세부 작업입니다. 해당 업무를 완료하고 결과물을 작업 내용에 등록해주세요.', DATE_ADD(NOW(), INTERVAL 12 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(1, 1, '수정 - 2025 문서 7', '2025 Q1 마케팅 캠페인의 세부 작업입니다. 해당 업무를 완료하고 결과물을 작업 내용에 등록해주세요.', DATE_ADD(NOW(), INTERVAL 18 DAY), 'IN_PROGRESS', NOW(), NOW()),
(1, 2, '개선 - 2025 프로세스 8', '2025 Q1 마케팅 캠페인의 세부 작업입니다. 해당 업무를 완료하고 결과물을 작업 내용에 등록해주세요.', DATE_ADD(NOW(), INTERVAL 25 DAY), 'CONFIRMED', NOW(), NOW()),
(1, 3, '최적화 - 2025 시스템 9', '2025 Q1 마케팅 캠페인의 세부 작업입니다. 해당 업무를 완료하고 결과물을 작업 내용에 등록해주세요.', DATE_ADD(NOW(), INTERVAL 7 DAY), 'IN_PROGRESS', NOW(), NOW()),
(1, 1, '조사 - 2025 요구사항 10', '2025 Q1 마케팅 캠페인의 세부 작업입니다. 해당 업무를 완료하고 결과물을 작업 내용에 등록해주세요.', DATE_ADD(NOW(), INTERVAL 14 DAY), 'WAITING_REVIEW', NOW(), NOW());

-- Task 2의 Todos
INSERT IGNORE INTO todos (task_id, assignee_id, title, description, due_date, status, created_at, updated_at) VALUES
(2, 2, '구현 - SNS 기능 1', 'SNS 콘텐츠 제작의 세부 작업입니다. 해당 업무를 완료하고 결과물을 작업 내용에 등록해주세요.', DATE_ADD(NOW(), INTERVAL 6 DAY), 'IN_PROGRESS', NOW(), NOW()),
(2, 1, '설계 - SNS 모듈 2', 'SNS 콘텐츠 제작의 세부 작업입니다. 해당 업무를 완료하고 결과물을 작업 내용에 등록해주세요.', DATE_ADD(NOW(), INTERVAL 11 DAY), 'CONFIRMED', NOW(), NOW()),
(2, 3, '테스트 - SNS 컴포넌트 3', 'SNS 콘텐츠 제작의 세부 작업입니다. 해당 업무를 완료하고 결과물을 작업 내용에 등록해주세요.', DATE_ADD(NOW(), INTERVAL 16 DAY), 'IN_PROGRESS', NOW(), NOW()),
(2, 2, '검토 - SNS API 4', 'SNS 콘텐츠 제작의 세부 작업입니다. 해당 업무를 완료하고 결과물을 작업 내용에 등록해주세요.', DATE_ADD(NOW(), INTERVAL 21 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(2, 1, '분석 - SNS UI 5', 'SNS 콘텐츠 제작의 세부 작업입니다. 해당 업무를 완료하고 결과물을 작업 내용에 등록해주세요.', DATE_ADD(NOW(), INTERVAL 9 DAY), 'CONFIRMED', NOW(), NOW()),
(2, 3, '작성 - SNS 데이터 6', 'SNS 콘텐츠 제작의 세부 작업입니다. 해당 업무를 완료하고 결과물을 작업 내용에 등록해주세요.', DATE_ADD(NOW(), INTERVAL 13 DAY), 'IN_PROGRESS', NOW(), NOW()),
(2, 2, '수정 - SNS 문서 7', 'SNS 콘텐츠 제작의 세부 작업입니다. 해당 업무를 완료하고 결과물을 작업 내용에 등록해주세요.', DATE_ADD(NOW(), INTERVAL 19 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(2, 1, '개선 - SNS 프로세스 8', 'SNS 콘텐츠 제작의 세부 작업입니다. 해당 업무를 완료하고 결과물을 작업 내용에 등록해주세요.', DATE_ADD(NOW(), INTERVAL 26 DAY), 'IN_PROGRESS', NOW(), NOW()),
(2, 3, '최적화 - SNS 시스템 9', 'SNS 콘텐츠 제작의 세부 작업입니다. 해당 업무를 완료하고 결과물을 작업 내용에 등록해주세요.', DATE_ADD(NOW(), INTERVAL 8 DAY), 'CONFIRMED', NOW(), NOW()),
(2, 2, '조사 - SNS 요구사항 10', 'SNS 콘텐츠 제작의 세부 작업입니다. 해당 업무를 완료하고 결과물을 작업 내용에 등록해주세요.', DATE_ADD(NOW(), INTERVAL 15 DAY), 'IN_PROGRESS', NOW(), NOW());

-- Task 3의 Todos
INSERT IGNORE INTO todos (task_id, assignee_id, title, description, due_date, status, created_at, updated_at) VALUES
(3, 1, '구현 - 브랜드 기능 1', '브랜드 리뉴얼의 세부 작업입니다.', DATE_ADD(NOW(), INTERVAL 7 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(3, 2, '설계 - 브랜드 모듈 2', '브랜드 리뉴얼의 세부 작업입니다.', DATE_ADD(NOW(), INTERVAL 12 DAY), 'IN_PROGRESS', NOW(), NOW()),
(3, 3, '테스트 - 브랜드 컴포넌트 3', '브랜드 리뉴얼의 세부 작업입니다.', DATE_ADD(NOW(), INTERVAL 17 DAY), 'CONFIRMED', NOW(), NOW()),
(3, 1, '검토 - 브랜드 API 4', '브랜드 리뉴얼의 세부 작업입니다.', DATE_ADD(NOW(), INTERVAL 22 DAY), 'IN_PROGRESS', NOW(), NOW()),
(3, 2, '분석 - 브랜드 UI 5', '브랜드 리뉴얼의 세부 작업입니다.', DATE_ADD(NOW(), INTERVAL 10 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(3, 3, '작성 - 브랜드 데이터 6', '브랜드 리뉴얼의 세부 작업입니다.', DATE_ADD(NOW(), INTERVAL 14 DAY), 'CONFIRMED', NOW(), NOW()),
(3, 1, '수정 - 브랜드 문서 7', '브랜드 리뉴얼의 세부 작업입니다.', DATE_ADD(NOW(), INTERVAL 20 DAY), 'IN_PROGRESS', NOW(), NOW()),
(3, 2, '개선 - 브랜드 프로세스 8', '브랜드 리뉴얼의 세부 작업입니다.', DATE_ADD(NOW(), INTERVAL 27 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(3, 3, '최적화 - 브랜드 시스템 9', '브랜드 리뉴얼의 세부 작업입니다.', DATE_ADD(NOW(), INTERVAL 9 DAY), 'CONFIRMED', NOW(), NOW()),
(3, 1, '조사 - 브랜드 요구사항 10', '브랜드 리뉴얼의 세부 작업입니다.', DATE_ADD(NOW(), INTERVAL 16 DAY), 'IN_PROGRESS', NOW(), NOW());

-- Task 4의 Todos (백엔드 API 개발)
INSERT IGNORE INTO todos (task_id, assignee_id, title, description, due_date, status, created_at, updated_at) VALUES
(4, 5, '구현 - 백엔드 기능 1', '백엔드 API 개발의 세부 작업입니다.', DATE_ADD(NOW(), INTERVAL 5 DAY), 'IN_PROGRESS', NOW(), NOW()),
(4, 4, '설계 - 백엔드 모듈 2', '백엔드 API 개발의 세부 작업입니다.', DATE_ADD(NOW(), INTERVAL 10 DAY), 'CONFIRMED', NOW(), NOW()),
(4, 6, '테스트 - 백엔드 컴포넌트 3', '백엔드 API 개발의 세부 작업입니다.', DATE_ADD(NOW(), INTERVAL 15 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(4, 7, '검토 - 백엔드 API 4', '백엔드 API 개발의 세부 작업입니다.', DATE_ADD(NOW(), INTERVAL 20 DAY), 'IN_PROGRESS', NOW(), NOW()),
(4, 5, '분석 - 백엔드 UI 5', '백엔드 API 개발의 세부 작업입니다.', DATE_ADD(NOW(), INTERVAL 8 DAY), 'CONFIRMED', NOW(), NOW()),
(4, 4, '작성 - 백엔드 데이터 6', '백엔드 API 개발의 세부 작업입니다.', DATE_ADD(NOW(), INTERVAL 12 DAY), 'IN_PROGRESS', NOW(), NOW()),
(4, 6, '수정 - 백엔드 문서 7', '백엔드 API 개발의 세부 작업입니다.', DATE_ADD(NOW(), INTERVAL 18 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(4, 7, '개선 - 백엔드 프로세스 8', '백엔드 API 개발의 세부 작업입니다.', DATE_ADD(NOW(), INTERVAL 25 DAY), 'CONFIRMED', NOW(), NOW()),
(4, 5, '최적화 - 백엔드 시스템 9', '백엔드 API 개발의 세부 작업입니다.', DATE_ADD(NOW(), INTERVAL 7 DAY), 'IN_PROGRESS', NOW(), NOW()),
(4, 4, '조사 - 백엔드 요구사항 10', '백엔드 API 개발의 세부 작업입니다.', DATE_ADD(NOW(), INTERVAL 14 DAY), 'WAITING_REVIEW', NOW(), NOW());

-- Task 5의 Todos (프론트엔드 개발)
INSERT IGNORE INTO todos (task_id, assignee_id, title, description, due_date, status, created_at, updated_at) VALUES
(5, 5, '프론트 작업 1', '프론트엔드 개발 작업', DATE_ADD(NOW(), INTERVAL 5 DAY), 'IN_PROGRESS', NOW(), NOW()),
(5, 4, '프론트 작업 2', '프론트엔드 개발 작업', DATE_ADD(NOW(), INTERVAL 10 DAY), 'CONFIRMED', NOW(), NOW()),
(5, 6, '프론트 작업 3', '프론트엔드 개발 작업', DATE_ADD(NOW(), INTERVAL 15 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(5, 7, '프론트 작업 4', '프론트엔드 개발 작업', DATE_ADD(NOW(), INTERVAL 20 DAY), 'IN_PROGRESS', NOW(), NOW()),
(5, 5, '프론트 작업 5', '프론트엔드 개발 작업', DATE_ADD(NOW(), INTERVAL 8 DAY), 'CONFIRMED', NOW(), NOW()),
(5, 4, '프론트 작업 6', '프론트엔드 개발 작업', DATE_ADD(NOW(), INTERVAL 12 DAY), 'IN_PROGRESS', NOW(), NOW()),
(5, 6, '프론트 작업 7', '프론트엔드 개발 작업', DATE_ADD(NOW(), INTERVAL 18 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(5, 7, '프론트 작업 8', '프론트엔드 개발 작업', DATE_ADD(NOW(), INTERVAL 25 DAY), 'CONFIRMED', NOW(), NOW()),
(5, 5, '프론트 작업 9', '프론트엔드 개발 작업', DATE_ADD(NOW(), INTERVAL 7 DAY), 'IN_PROGRESS', NOW(), NOW()),
(5, 4, '프론트 작업 10', '프론트엔드 개발 작업', DATE_ADD(NOW(), INTERVAL 14 DAY), 'WAITING_REVIEW', NOW(), NOW());

-- Task 6의 Todos (데이터베이스 최적화)
INSERT IGNORE INTO todos (task_id, assignee_id, title, description, due_date, status, created_at, updated_at) VALUES
(6, 4, 'DB 최적화 1', '데이터베이스 최적화 작업', DATE_ADD(NOW(), INTERVAL 5 DAY), 'CONFIRMED', NOW(), NOW()),
(6, 5, 'DB 최적화 2', '데이터베이스 최적화 작업', DATE_ADD(NOW(), INTERVAL 10 DAY), 'IN_PROGRESS', NOW(), NOW()),
(6, 6, 'DB 최적화 3', '데이터베이스 최적화 작업', DATE_ADD(NOW(), INTERVAL 15 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(6, 7, 'DB 최적화 4', '데이터베이스 최적화 작업', DATE_ADD(NOW(), INTERVAL 20 DAY), 'CONFIRMED', NOW(), NOW()),
(6, 4, 'DB 최적화 5', '데이터베이스 최적화 작업', DATE_ADD(NOW(), INTERVAL 8 DAY), 'IN_PROGRESS', NOW(), NOW()),
(6, 5, 'DB 최적화 6', '데이터베이스 최적화 작업', DATE_ADD(NOW(), INTERVAL 12 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(6, 6, 'DB 최적화 7', '데이터베이스 최적화 작업', DATE_ADD(NOW(), INTERVAL 18 DAY), 'CONFIRMED', NOW(), NOW()),
(6, 7, 'DB 최적화 8', '데이터베이스 최적화 작업', DATE_ADD(NOW(), INTERVAL 25 DAY), 'IN_PROGRESS', NOW(), NOW()),
(6, 4, 'DB 최적화 9', '데이터베이스 최적화 작업', DATE_ADD(NOW(), INTERVAL 7 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(6, 5, 'DB 최적화 10', '데이터베이스 최적화 작업', DATE_ADD(NOW(), INTERVAL 14 DAY), 'CONFIRMED', NOW(), NOW());

-- Task 7의 Todos (모바일 앱 UI 디자인)
INSERT IGNORE INTO todos (task_id, assignee_id, title, description, due_date, status, created_at, updated_at) VALUES
(7, 8, '모바일 UI 1', '모바일 앱 UI 디자인 작업', DATE_ADD(NOW(), INTERVAL 5 DAY), 'IN_PROGRESS', NOW(), NOW()),
(7, 7, '모바일 UI 2', '모바일 앱 UI 디자인 작업', DATE_ADD(NOW(), INTERVAL 10 DAY), 'CONFIRMED', NOW(), NOW()),
(7, 9, '모바일 UI 3', '모바일 앱 UI 디자인 작업', DATE_ADD(NOW(), INTERVAL 15 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(7, 10, '모바일 UI 4', '모바일 앱 UI 디자인 작업', DATE_ADD(NOW(), INTERVAL 20 DAY), 'IN_PROGRESS', NOW(), NOW()),
(7, 8, '모바일 UI 5', '모바일 앱 UI 디자인 작업', DATE_ADD(NOW(), INTERVAL 8 DAY), 'CONFIRMED', NOW(), NOW()),
(7, 7, '모바일 UI 6', '모바일 앱 UI 디자인 작업', DATE_ADD(NOW(), INTERVAL 12 DAY), 'IN_PROGRESS', NOW(), NOW()),
(7, 9, '모바일 UI 7', '모바일 앱 UI 디자인 작업', DATE_ADD(NOW(), INTERVAL 18 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(7, 10, '모바일 UI 8', '모바일 앱 UI 디자인 작업', DATE_ADD(NOW(), INTERVAL 25 DAY), 'CONFIRMED', NOW(), NOW()),
(7, 8, '모바일 UI 9', '모바일 앱 UI 디자인 작업', DATE_ADD(NOW(), INTERVAL 7 DAY), 'IN_PROGRESS', NOW(), NOW()),
(7, 7, '모바일 UI 10', '모바일 앱 UI 디자인 작업', DATE_ADD(NOW(), INTERVAL 14 DAY), 'WAITING_REVIEW', NOW(), NOW());

-- Task 8의 Todos (웹사이트 리디자인)
INSERT IGNORE INTO todos (task_id, assignee_id, title, description, due_date, status, created_at, updated_at) VALUES
(8, 8, '웹 리디자인 1', '웹사이트 리디자인 작업', DATE_ADD(NOW(), INTERVAL 6 DAY), 'CONFIRMED', NOW(), NOW()),
(8, 7, '웹 리디자인 2', '웹사이트 리디자인 작업', DATE_ADD(NOW(), INTERVAL 11 DAY), 'IN_PROGRESS', NOW(), NOW()),
(8, 9, '웹 리디자인 3', '웹사이트 리디자인 작업', DATE_ADD(NOW(), INTERVAL 16 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(8, 10, '웹 리디자인 4', '웹사이트 리디자인 작업', DATE_ADD(NOW(), INTERVAL 21 DAY), 'CONFIRMED', NOW(), NOW()),
(8, 8, '웹 리디자인 5', '웹사이트 리디자인 작업', DATE_ADD(NOW(), INTERVAL 9 DAY), 'IN_PROGRESS', NOW(), NOW()),
(8, 7, '웹 리디자인 6', '웹사이트 리디자인 작업', DATE_ADD(NOW(), INTERVAL 13 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(8, 9, '웹 리디자인 7', '웹사이트 리디자인 작업', DATE_ADD(NOW(), INTERVAL 19 DAY), 'CONFIRMED', NOW(), NOW()),
(8, 10, '웹 리디자인 8', '웹사이트 리디자인 작업', DATE_ADD(NOW(), INTERVAL 26 DAY), 'IN_PROGRESS', NOW(), NOW()),
(8, 8, '웹 리디자인 9', '웹사이트 리디자인 작업', DATE_ADD(NOW(), INTERVAL 8 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(8, 7, '웹 리디자인 10', '웹사이트 리디자인 작업', DATE_ADD(NOW(), INTERVAL 15 DAY), 'CONFIRMED', NOW(), NOW());

-- Task 9의 Todos (디자인 시스템 구축)
INSERT IGNORE INTO todos (task_id, assignee_id, title, description, due_date, status, created_at, updated_at) VALUES
(9, 7, '디자인시스템 1', '디자인 시스템 구축 작업', DATE_ADD(NOW(), INTERVAL 7 DAY), 'IN_PROGRESS', NOW(), NOW()),
(9, 8, '디자인시스템 2', '디자인 시스템 구축 작업', DATE_ADD(NOW(), INTERVAL 12 DAY), 'CONFIRMED', NOW(), NOW()),
(9, 9, '디자인시스템 3', '디자인 시스템 구축 작업', DATE_ADD(NOW(), INTERVAL 17 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(9, 10, '디자인시스템 4', '디자인 시스템 구축 작업', DATE_ADD(NOW(), INTERVAL 22 DAY), 'IN_PROGRESS', NOW(), NOW()),
(9, 7, '디자인시스템 5', '디자인 시스템 구축 작업', DATE_ADD(NOW(), INTERVAL 10 DAY), 'CONFIRMED', NOW(), NOW()),
(9, 8, '디자인시스템 6', '디자인 시스템 구축 작업', DATE_ADD(NOW(), INTERVAL 14 DAY), 'IN_PROGRESS', NOW(), NOW()),
(9, 9, '디자인시스템 7', '디자인 시스템 구축 작업', DATE_ADD(NOW(), INTERVAL 20 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(9, 10, '디자인시스템 8', '디자인 시스템 구축 작업', DATE_ADD(NOW(), INTERVAL 27 DAY), 'CONFIRMED', NOW(), NOW()),
(9, 7, '디자인시스템 9', '디자인 시스템 구축 작업', DATE_ADD(NOW(), INTERVAL 9 DAY), 'IN_PROGRESS', NOW(), NOW()),
(9, 8, '디자인시스템 10', '디자인 시스템 구축 작업', DATE_ADD(NOW(), INTERVAL 16 DAY), 'WAITING_REVIEW', NOW(), NOW());

-- TodoWork 샘플 데이터 (완료된 Todo에 작업 내용 추가)
INSERT IGNORE INTO todo_work (todo_id, author_id, title, content, created_at, updated_at) VALUES
(1, 1, '작업 진행 상황 #1', '기본 구조를 설계하고 초안을 작성했습니다. 주요 기능 명세를 정리했으며, 다음 단계로 진행 가능합니다.', NOW(), NOW()),
(1, 1, '작업 진행 상황 #2', '최종 검토 및 리팩토링을 완료했습니다. 문서화도 함께 진행했으며, 배포 준비가 되었습니다.', NOW(), NOW()),
(4, 1, '작업 진행 상황 #1', '핵심 기능을 구현했습니다. 단위 테스트도 함께 작성했으며, 코드 리뷰 준비가 완료되었습니다.', NOW(), NOW()),
(8, 2, '작업 진행 상황 #1', '요구사항 분석을 완료했습니다. 기술 스택을 선정하고 아키텍처를 설계했습니다.', NOW(), NOW());

-- TodoComment 샘플 데이터 (일부 Todo에 댓글 추가)
INSERT IGNORE INTO todo_comments (todo_id, author_id, content, created_at, updated_at) VALUES
(1, 2, '좋은 진행 상황입니다! 계속해서 진행해주세요.', NOW(), NOW()),
(1, 3, '확인했습니다. 몇 가지 수정사항이 있어서 별도로 말씀드리겠습니다.', NOW(), NOW()),
(2, 1, '일정이 조금 타이트한데 괜찮으신가요? 필요하면 지원하겠습니다.', NOW(), NOW()),
(3, 2, '훌륭한 작업입니다! 다음 단계로 넘어가도 좋을 것 같습니다.', NOW(), NOW()),
(5, 3, '테스트 결과 이상 없습니다. 승인합니다.', NOW(), NOW());
