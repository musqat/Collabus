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
(2, 2, 'MANAGER', NOW(), NOW()),
(2, 5, 'MASTER', NOW(), NOW()),
(2, 6, 'MANAGER', NOW(), NOW()),
(2, 7, 'MEMBER', NOW(), NOW()),
(2, 8, 'MEMBER', NOW(), NOW());

-- 디자인팀 (workspace_id=3)
INSERT IGNORE INTO workspace_user (workspace_id, user_id, role, created_at, updated_at) VALUES
(3, 1, 'MASTER', NOW(), NOW()),
(3, 2, 'MEMBER', NOW(), NOW()),
(3, 8, 'MASTER', NOW(), NOW()),
(3, 9, 'MANAGER', NOW(), NOW()),
(3, 10, 'MEMBER', NOW(), NOW());

-- Task 생성 (각 Workspace에 3개씩, 총 9개)
-- 마케팅팀 Tasks
INSERT IGNORE INTO task (workspace_id, title, description, task_manager_id, due_date, created_at, updated_at) VALUES
(1, '하반기 브랜드 캠페인', '신규 슬로건을 중심으로 한 하반기 통합 캠페인', 2, DATE_ADD(CURDATE(), INTERVAL 30 DAY), NOW(), NOW()),
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

-- Todos 생성 (각 Task 마다 10개, 총 90개)
-- Task 1 — 하반기 브랜드 캠페인
INSERT IGNORE INTO todos (task_id, assignee_id, title, description, due_date, status, created_at, updated_at) VALUES
(1, 1, '캠페인 콘셉트 확정', '슬로건 후보 3안 중 최종안을 정하고 근거를 정리한다.', DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'CONFIRMED', NOW(), NOW()),
(1, 2, '타깃 페르소나 정의', '20-30대 직장인 기준으로 페르소나 2종을 문서화한다.', DATE_ADD(CURDATE(), INTERVAL 8 DAY), 'CONFIRMED', NOW(), NOW()),
(1, 3, '매체 예산 배분안', '검색·SNS·옥외 비중을 잡고 예상 도달률을 계산한다.', DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(1, 1, '키비주얼 시안 검토', '디자인팀 1차 시안에 피드백을 정리해 전달한다.', DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'IN_PROGRESS', NOW(), NOW()),
(1, 2, '랜딩페이지 카피 작성', '메인 헤드라인과 CTA 문구를 각 2안씩 준비한다.', DATE_ADD(CURDATE(), INTERVAL 9 DAY), 'IN_PROGRESS', NOW(), NOW()),
(1, 3, '인플루언서 리스트업', '팔로워 규모별로 후보 20명을 추리고 단가를 조사한다.', DATE_ADD(CURDATE(), INTERVAL 12 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(1, 1, '경쟁사 캠페인 분석', '최근 6개월 경쟁사 3곳의 집행 사례를 정리한다.', DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'CONFIRMED', NOW(), NOW()),
(1, 2, '성과 지표 정의', '노출·클릭·전환 중 무엇을 주지표로 볼지 합의한다.', DATE_ADD(CURDATE(), INTERVAL 14 DAY), 'IN_PROGRESS', NOW(), NOW()),
(1, 3, '법무 검토 요청', '문구에 과장 광고 소지가 없는지 확인받는다.', DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'IN_PROGRESS', NOW(), NOW()),
(1, 1, '킥오프 미팅 자료', '전체 일정과 역할 분담을 한 장으로 정리한다.', DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'WAITING_REVIEW', NOW(), NOW());

-- Task 2 — SNS 콘텐츠 제작
INSERT IGNORE INTO todos (task_id, assignee_id, title, description, due_date, status, created_at, updated_at) VALUES
(2, 2, '월간 콘텐츠 캘린더', '이번 달 게시 일정과 채널별 포맷을 확정한다.', DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'CONFIRMED', NOW(), NOW()),
(2, 3, '릴스 기획안 5편', '짧은 호흡의 후킹 구조로 초안을 잡는다.', DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'IN_PROGRESS', NOW(), NOW()),
(2, 1, '촬영 소품 준비', '제품 컷에 쓸 배경지와 소품을 목록화해 구매한다.', DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'CONFIRMED', NOW(), NOW()),
(2, 2, '썸네일 비교 테스트', '동일 영상에 썸네일 2종을 걸어 클릭률을 비교한다.', DATE_ADD(CURDATE(), INTERVAL 9 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(2, 3, '해시태그 세트 정리', '브랜드·캠페인·일반 태그를 묶어 3세트로 만든다.', DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'IN_PROGRESS', NOW(), NOW()),
(2, 1, '댓글 응대 가이드', '자주 오는 질문 유형별 답변 톤을 정한다.', DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'CONFIRMED', NOW(), NOW()),
(2, 2, '쇼츠 재편집', '기존 장편 영상에서 하이라이트를 잘라낸다.', DATE_ADD(CURDATE(), INTERVAL 11 DAY), 'IN_PROGRESS', NOW(), NOW()),
(2, 3, '협업 계정 섭외', '결이 맞는 브랜드 계정 3곳에 제안서를 보낸다.', DATE_ADD(CURDATE(), INTERVAL 13 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(2, 1, '주간 성과 리포트', '채널별 도달과 저장 수를 표로 정리한다.', DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'IN_PROGRESS', NOW(), NOW()),
(2, 2, '이미지 저작권 확인', '사용한 폰트와 스톡 이미지의 라이선스를 점검한다.', DATE_ADD(CURDATE(), INTERVAL 10 DAY), 'WAITING_REVIEW', NOW(), NOW());

-- Task 3 — 브랜드 리뉴얼
INSERT IGNORE INTO todos (task_id, assignee_id, title, description, due_date, status, created_at, updated_at) VALUES
(3, 3, '현행 로고 사용처 조사', '온·오프라인에 쓰이는 로고 위치를 전수 조사한다.', DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'CONFIRMED', NOW(), NOW()),
(3, 1, '브랜드 인지도 설문', '기존 고객 200명 대상 설문 문항을 설계한다.', DATE_ADD(CURDATE(), INTERVAL 12 DAY), 'IN_PROGRESS', NOW(), NOW()),
(3, 2, '컬러 팔레트 후보', '주조색 2안과 보조색 조합을 준비한다.', DATE_ADD(CURDATE(), INTERVAL 8 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(3, 3, '서체 선정', '국문·영문 서체를 한 쌍으로 묶어 비교한다.', DATE_ADD(CURDATE(), INTERVAL 15 DAY), 'IN_PROGRESS', NOW(), NOW()),
(3, 1, '네이밍 검토', '상표 등록 가능 여부를 후보별로 확인한다.', DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'CONFIRMED', NOW(), NOW()),
(3, 2, '브랜드북 목차 구성', '적용 원칙과 금지 사례를 어디까지 담을지 정한다.', DATE_ADD(CURDATE(), INTERVAL 20 DAY), 'IN_PROGRESS', NOW(), NOW()),
(3, 3, '명함·봉투 시안', '새 아이덴티티를 적용한 인쇄물 시안을 만든다.', DATE_ADD(CURDATE(), INTERVAL 18 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(3, 1, '사내 공유 발표', '리뉴얼 배경과 방향을 전사에 설명한다.', DATE_ADD(CURDATE(), INTERVAL 25 DAY), 'IN_PROGRESS', NOW(), NOW()),
(3, 2, '웹 적용 범위 산정', '리뉴얼 시 손봐야 할 페이지 수를 추린다.', DATE_ADD(CURDATE(), INTERVAL 22 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(3, 3, '인쇄 업체 견적', '제작 수량별 단가를 세 곳에서 받는다.', DATE_ADD(CURDATE(), INTERVAL 9 DAY), 'CONFIRMED', NOW(), NOW());

-- Task 4 — 백엔드 API 개발
INSERT IGNORE INTO todos (task_id, assignee_id, title, description, due_date, status, created_at, updated_at) VALUES
(4, 4, '인증 토큰 만료 처리', 'Access Token 만료 시 재발급 흐름을 정리한다.', DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'CONFIRMED', NOW(), NOW()),
(4, 5, '페이지네이션 응답 통일', '목록 API 가 같은 형태로 내려주도록 맞춘다.', DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'CONFIRMED', NOW(), NOW()),
(4, 6, '권한 검사 공통화', '컨트롤러마다 흩어진 검사를 한곳으로 모은다.', DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(4, 4, '파일 업로드 제한', '확장자 허용 목록과 용량 상한을 적용한다.', DATE_ADD(CURDATE(), INTERVAL 9 DAY), 'IN_PROGRESS', NOW(), NOW()),
(4, 5, '예외 응답 형식 정리', '에러 코드와 메시지 구조를 문서화한다.', DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'IN_PROGRESS', NOW(), NOW()),
(4, 6, '알림 발송 비동기화', '트랜잭션 커밋 이후에 알림이 나가도록 바꾼다.', DATE_ADD(CURDATE(), INTERVAL 11 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(4, 4, 'API 문서 보강', '요청·응답 예시를 엔드포인트마다 채운다.', DATE_ADD(CURDATE(), INTERVAL 8 DAY), 'IN_PROGRESS', NOW(), NOW()),
(4, 5, '검색 조건 확장', '키워드와 상태를 함께 걸 수 있게 한다.', DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'CONFIRMED', NOW(), NOW()),
(4, 6, '소프트 삭제 도입', '탈퇴 사용자의 작성물을 남기는 방식으로 바꾼다.', DATE_ADD(CURDATE(), INTERVAL 13 DAY), 'IN_PROGRESS', NOW(), NOW()),
(4, 4, '통합 테스트 추가', '로그인부터 보호 API 호출까지 한 번에 검증한다.', DATE_ADD(CURDATE(), INTERVAL 10 DAY), 'WAITING_REVIEW', NOW(), NOW());

-- Task 5 — 프론트엔드 개발
INSERT IGNORE INTO todos (task_id, assignee_id, title, description, due_date, status, created_at, updated_at) VALUES
(5, 5, '목록 응답 처리 수정', '페이지 객체를 배열로 다루던 부분을 바로잡는다.', DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'CONFIRMED', NOW(), NOW()),
(5, 6, '사이드바 트리 개선', '워크스페이스 아래 Task 와 Todo 를 펼쳐 보여준다.', DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'CONFIRMED', NOW(), NOW()),
(5, 4, '정렬 드롭다운 추가', '마감일·상태·제목 기준 정렬을 붙인다.', DATE_ADD(CURDATE(), INTERVAL 8 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(5, 5, '토스트 알림 전환', '브라우저 기본 경고창을 토스트로 바꾼다.', DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'IN_PROGRESS', NOW(), NOW()),
(5, 6, '에러 경계 적용', '화면 하나가 죽어도 전체가 백지가 되지 않게 한다.', DATE_ADD(CURDATE(), INTERVAL 9 DAY), 'IN_PROGRESS', NOW(), NOW()),
(5, 4, '날짜 표기 통일', '마감일 색과 문구 기준을 한곳에서 정한다.', DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'CONFIRMED', NOW(), NOW()),
(5, 5, '검색 입력 지연 처리', '타이핑 중 요청이 매번 나가지 않도록 늦춘다.', DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(5, 6, '반응형 레이아웃 점검', '좁은 화면에서 표가 넘치는 부분을 손본다.', DATE_ADD(CURDATE(), INTERVAL 12 DAY), 'IN_PROGRESS', NOW(), NOW()),
(5, 4, '로그인 화면 개편', '제품 소개와 데모 진입을 함께 담는다.', DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'IN_PROGRESS', NOW(), NOW()),
(5, 5, '단위 테스트 도입', '유틸과 훅부터 검사 범위를 넓힌다.', DATE_ADD(CURDATE(), INTERVAL 14 DAY), 'WAITING_REVIEW', NOW(), NOW());

-- Task 6 — 데이터베이스 최적화
INSERT IGNORE INTO todos (task_id, assignee_id, title, description, due_date, status, created_at, updated_at) VALUES
(6, 6, '느린 쿼리 수집', '실행 시간 상위 20개를 뽑아 원인을 적는다.', DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'CONFIRMED', NOW(), NOW()),
(6, 4, '연관 조회 묶기', '목록에서 연관 데이터를 한 번에 가져오도록 바꾼다.', DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'CONFIRMED', NOW(), NOW()),
(6, 5, '인덱스 재설계', '조회 조건에 맞춰 복합 인덱스를 다시 잡는다.', DATE_ADD(CURDATE(), INTERVAL 8 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(6, 6, '마이그레이션 도입', '스키마 변경 이력을 파일로 관리한다.', DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'IN_PROGRESS', NOW(), NOW()),
(6, 4, '삭제 규칙 정리', '하위 데이터 정리를 제약으로 내린다.', DATE_ADD(CURDATE(), INTERVAL 10 DAY), 'IN_PROGRESS', NOW(), NOW()),
(6, 5, '커넥션 풀 조정', '동시 요청 수에 맞춰 최대 연결을 정한다.', DATE_ADD(CURDATE(), INTERVAL 12 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(6, 6, '날짜 타입 통일', '시각이 필요 없는 컬럼을 날짜로 바꾼다.', DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'CONFIRMED', NOW(), NOW()),
(6, 4, '백업 절차 수립', '덤프와 복원을 실제로 한 번 해본다.', DATE_ADD(CURDATE(), INTERVAL 15 DAY), 'IN_PROGRESS', NOW(), NOW()),
(6, 5, '통계 쿼리 캐싱', '진행률 집계를 매번 계산하지 않게 한다.', DATE_ADD(CURDATE(), INTERVAL 9 DAY), 'IN_PROGRESS', NOW(), NOW()),
(6, 6, '장애 시나리오 점검', 'DB 가 잠깐 끊길 때 앱이 어떻게 되는지 본다.', DATE_ADD(CURDATE(), INTERVAL 18 DAY), 'WAITING_REVIEW', NOW(), NOW());

-- Task 7 — 모바일 앱 UI 디자인
INSERT IGNORE INTO todos (task_id, assignee_id, title, description, due_date, status, created_at, updated_at) VALUES
(7, 7, '온보딩 3단계 설계', '첫 실행에서 무엇을 먼저 보여줄지 정한다.', DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'CONFIRMED', NOW(), NOW()),
(7, 8, '하단 탭 구조 확정', '탭 4개와 각 진입점을 정리한다.', DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'CONFIRMED', NOW(), NOW()),
(7, 9, '다크 모드 대응', '색 토큰을 두 벌로 나눠 정의한다.', DATE_ADD(CURDATE(), INTERVAL 11 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(7, 7, '빈 화면 일러스트', '데이터가 없을 때 보여줄 그림 4종을 그린다.', DATE_ADD(CURDATE(), INTERVAL 9 DAY), 'IN_PROGRESS', NOW(), NOW()),
(7, 8, '제스처 정의', '밀어서 완료·삭제 동작의 기준을 정한다.', DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'IN_PROGRESS', NOW(), NOW()),
(7, 9, '접근성 대비 점검', '글자와 배경의 명도 대비를 기준에 맞춘다.', DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'CONFIRMED', NOW(), NOW()),
(7, 7, '아이콘 세트 정리', '선 두께와 모서리 반경을 하나로 맞춘다.', DATE_ADD(CURDATE(), INTERVAL 8 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(7, 8, '알림 화면 설계', '읽음과 안 읽음을 어떻게 구분할지 정한다.', DATE_ADD(CURDATE(), INTERVAL 13 DAY), 'IN_PROGRESS', NOW(), NOW()),
(7, 9, '프로토타입 연결', '주요 흐름을 눌러볼 수 있게 잇는다.', DATE_ADD(CURDATE(), INTERVAL 10 DAY), 'IN_PROGRESS', NOW(), NOW()),
(7, 7, '개발 전달 문서', '간격과 색 값을 표로 정리해 넘긴다.', DATE_ADD(CURDATE(), INTERVAL 16 DAY), 'WAITING_REVIEW', NOW(), NOW());

-- Task 8 — 웹사이트 리디자인
INSERT IGNORE INTO todos (task_id, assignee_id, title, description, due_date, status, created_at, updated_at) VALUES
(8, 8, '정보 구조 재설계', '메뉴를 3단계 이내로 줄이는 안을 만든다.', DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'CONFIRMED', NOW(), NOW()),
(8, 9, '메인 히어로 시안', '첫 화면에서 무엇을 말할지 2안으로 준비한다.', DATE_ADD(CURDATE(), INTERVAL 8 DAY), 'IN_PROGRESS', NOW(), NOW()),
(8, 7, '반응형 그리드 정의', '데스크톱·태블릿·모바일 기준점을 정한다.', DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'CONFIRMED', NOW(), NOW()),
(8, 8, '사례 페이지 템플릿', '고객 사례를 같은 틀로 보여줄 형식을 만든다.', DATE_ADD(CURDATE(), INTERVAL 12 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(8, 9, '폼 검증 표시 방식', '오류를 언제 어디에 띄울지 정한다.', DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'IN_PROGRESS', NOW(), NOW()),
(8, 7, '이미지 최적화 기준', '해상도와 포맷 규칙을 정해 용량을 줄인다.', DATE_ADD(CURDATE(), INTERVAL 9 DAY), 'IN_PROGRESS', NOW(), NOW()),
(8, 8, '검색 메타 정리', '페이지별 제목과 설명 문구를 채운다.', DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'CONFIRMED', NOW(), NOW()),
(8, 9, '푸터 정보 정비', '회사 정보와 링크를 최신으로 맞춘다.', DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(8, 7, '로딩 상태 디자인', '느린 구간에서 무엇을 보여줄지 정한다.', DATE_ADD(CURDATE(), INTERVAL 11 DAY), 'IN_PROGRESS', NOW(), NOW()),
(8, 8, '브라우저 호환 점검', '구형 브라우저에서 깨지는 부분을 찾는다.', DATE_ADD(CURDATE(), INTERVAL 15 DAY), 'WAITING_REVIEW', NOW(), NOW());

-- Task 9 — 디자인 시스템 구축
INSERT IGNORE INTO todos (task_id, assignee_id, title, description, due_date, status, created_at, updated_at) VALUES
(9, 9, '컬러 토큰 정의', '브랜드색과 상태색을 역할로 나눠 이름 붙인다.', DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'CONFIRMED', NOW(), NOW()),
(9, 7, '타이포 스케일 확정', '제목부터 본문까지 크기 단계를 정한다.', DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'CONFIRMED', NOW(), NOW()),
(9, 8, '버튼 상태 정리', '기본·호버·비활성·로딩을 한 벌로 만든다.', DATE_ADD(CURDATE(), INTERVAL 8 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(9, 9, '입력 컴포넌트 통일', '테두리와 포커스 표시를 하나로 맞춘다.', DATE_ADD(CURDATE(), INTERVAL 10 DAY), 'IN_PROGRESS', NOW(), NOW()),
(9, 7, '간격 규칙 수립', '4의 배수로 여백 단계를 제한한다.', DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'IN_PROGRESS', NOW(), NOW()),
(9, 8, '아이콘 사용 기준', '어떤 크기에 어떤 두께를 쓸지 정한다.', DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'CONFIRMED', NOW(), NOW()),
(9, 9, '컴포넌트 문서화', '쓰는 법과 쓰면 안 되는 경우를 함께 적는다.', DATE_ADD(CURDATE(), INTERVAL 14 DAY), 'IN_PROGRESS', NOW(), NOW()),
(9, 7, '모달·토스트 규칙', '무엇을 모달로, 무엇을 토스트로 할지 나눈다.', DATE_ADD(CURDATE(), INTERVAL 12 DAY), 'WAITING_REVIEW', NOW(), NOW()),
(9, 8, '표 컴포넌트 설계', '정렬과 페이지 이동을 포함한 형태를 잡는다.', DATE_ADD(CURDATE(), INTERVAL 16 DAY), 'IN_PROGRESS', NOW(), NOW()),
(9, 9, '적용 현황 점검', '기존 화면 중 몇 퍼센트가 옮겨졌는지 센다.', DATE_ADD(CURDATE(), INTERVAL 20 DAY), 'WAITING_REVIEW', NOW(), NOW());

-- 작업 내용 샘플. 완료·검수 대기 상태의 Todo 에 붙인다
INSERT IGNORE INTO todo_work (todo_id, author_id, title, content, created_at, updated_at) VALUES
(1, 1, '슬로건 최종안 정리', '3안 중 B안으로 정했습니다. 인지도 조사에서 회상률이 가장 높았고, 영문 병기도 자연스럽습니다. 근거 자료는 첨부에 넣었습니다.', NOW(), NOW()),
(1, 1, '임원 보고 반영', '보고 자리에서 나온 의견을 반영해 부제를 한 줄 줄였습니다. 최종본으로 확정합니다.', NOW(), NOW()),
(2, 2, '페르소나 초안', '주 사용자 2명으로 좁혔습니다. 하나는 팀장급, 하나는 실무자입니다. 각각의 하루 일과를 기준으로 접점을 정리했습니다.', NOW(), NOW()),
(3, 3, '매체별 예상 도달', '검색 40, SNS 45, 옥외 15 비중으로 잡았을 때 예상 도달이 가장 큽니다. 옥외는 지역을 두 곳으로 줄였습니다.', NOW(), NOW()),
(31, 4, '재발급 흐름 정리', '만료 시 저장된 토큰과 대조한 뒤 새 쌍을 발급하고 기존 것을 무효화합니다. 동시 요청은 한 번만 재발급하도록 큐로 묶었습니다.', NOW(), NOW()),
(32, 5, '응답 형태 통일', '목록 API 가 모두 같은 페이지 객체를 내려주도록 맞췄습니다. 프론트에서 벗기는 깊이도 하나로 줄었습니다.', NOW(), NOW()),
(41, 5, '원인 확인', '페이지 객체를 배열로 다루고 있었습니다. 에러가 나지 않고 조용히 빈 값이 되어 화면에서만 티가 났습니다.', NOW(), NOW()),
(61, 7, '온보딩 3단계 구성', '워크스페이스 만들기, 팀원 초대, 첫 Task 등록 순으로 잡았습니다. 각 단계는 건너뛸 수 있게 합니다.', NOW(), NOW());

-- 댓글 샘플. 검수와 협의가 오가는 흐름을 보여준다
INSERT IGNORE INTO todo_comments (todo_id, author_id, content, created_at, updated_at) VALUES
(1, 2, 'B안 좋습니다. 영문 병기까지 확인했고 이대로 진행하시죠.', NOW(), NOW()),
(3, 1, '옥외 비중이 조금 높아 보이는데, 지역을 두 곳으로 줄이면 어떨까요?', NOW(), NOW()),
(3, 3, '말씀대로 두 곳으로 줄였습니다. 도달은 크게 안 떨어집니다.', NOW(), NOW()),
(4, 2, '시안 피드백에 여백 관련 의견도 넣어주시면 좋겠습니다.', NOW(), NOW()),
(31, 5, '동시 요청 처리까지 들어간 점 좋네요. 승인합니다.', NOW(), NOW()),
(33, 6, '공통화하면서 기존 검사 로직이 빠진 곳은 없는지 한 번만 더 봐주세요.', NOW(), NOW()),
(41, 6, '같은 원인으로 진행률도 0으로 나오고 있었습니다. 함께 고쳤습니다.', NOW(), NOW()),
(63, 9, '다크 모드는 색만 바꾸면 대비가 깨지는 곳이 있어 토큰부터 나누는 게 맞습니다.', NOW(), NOW());
