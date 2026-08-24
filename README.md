# Collabus

> 팀 협업 작업 관리 플랫폼. Workspace · Task · Todo 계층 구조 기반

[![CI](https://github.com/musqat/Collabus/actions/workflows/ci.yml/badge.svg)](https://github.com/musqat/Collabus/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-007396)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F)
![React](https://img.shields.io/badge/React-18-61DAFB)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1)
![Redis](https://img.shields.io/badge/Redis-7-DC382D)
![tests](https://img.shields.io/badge/tests-backend_301_%C2%B7_frontend_88_%C2%B7_e2e_18-success)

워크스페이스에서 Task 를 나누고, Task 안의 Todo 를 담당자에게 맡겨
`IN_PROGRESS → WAITING_REVIEW → CONFIRMED` 로 검수까지 끌고 갑니다.
권한은 MASTER · MANAGER · MEMBER 세 단계이며, 상태가 바뀌면 WebSocket 으로 알림이 갑니다.

---

## Demo

> 데모 로그인 → 대시보드 → 워크스페이스 → Task → Todo 확인 → 검수 승인 → 담당자 변경 → 멤버 관리

<img src="docs/screenshots/Animation.gif" width="100%" alt="Collabus Demo"/>

---

## Tech Stack

| Layer | Stack |
|-------|-------|
| Backend | Java 21 · Spring Boot 3 · Spring Security · JWT · WebSocket (STOMP, 알림 푸시) |
| Frontend | React 18 · Vite · React Router v6 · TanStack Query · Zustand · Tailwind CSS |
| Database | MySQL 8.0 |
| Cache | Redis 7 — Refresh Token · 로그인 실패 횟수 · 블랙리스트 |
| Infra | Docker · Docker Compose · Nginx |

---

<details>
<summary><b>Test Accounts</b> — 데모 계정과 워크스페이스별 권한</summary>

더미 데이터가 자동으로 삽입됩니다. 아래 계정으로 바로 로그인 가능합니다.

로그인 화면의 **데모 계정으로 둘러보기** 를 누르면 `user1@test.com` 으로 바로 들어갑니다.

| 이메일 | 비밀번호 | 소속 워크스페이스 |
|--------|---------|-----------------|
| `user1@test.com` | `password` | 마케팅팀 (MASTER) · 개발팀 (MANAGER) · 디자인팀 (MEMBER) |
| `user2@test.com` | `password` | 마케팅팀 (MANAGER) |
| `user3@test.com` | `password` | 마케팅팀 (MEMBER) |
| `user4@test.com` | `password` | 개발팀 (MASTER) |
| `user7@test.com` | `password` | 개발팀 (MEMBER) · 디자인팀 (MASTER) |
| `admin@collabus.com` | `password` | 세 워크스페이스 모두 (MASTER) |

> `user1` ~ `user9` 모두 비밀번호 `password` 동일
>
> `user1` 은 워크스페이스마다 권한이 달라 MASTER·MANAGER·MEMBER 가 각각 무엇을
> 할 수 있는지 한 계정으로 확인할 수 있습니다.

</details>

---

<details>
<summary><b>Quick Start</b> — Docker 실행, 로컬 개발, 테스트</summary>

### Docker (권장)

```bash
# 1. 환경변수 설정
cp .env.example .env

# 2. 실행
docker compose up --build -d

# 3. 종료
docker compose down
```

| 서비스 | URL |
|--------|-----|
| 앱 | http://localhost |
| API | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui/index.html |

### 로컬

> Node 22.22.2 이상이 필요합니다. 낮은 버전에서는 `npm ci` 가 `EBADENGINE` 으로 막힙니다.

```bash
# 1. MySQL, Redis 컨테이너만 실행 (MySQL 은 호스트 3307 포트)
docker compose up -d mysql redis

# 2. 백엔드
cp .env.example .env
./gradlew bootRun

# 3. 프론트엔드
cd frontend
cp .env.example .env
npm install && npm run dev
```

| 서비스 | URL |
|--------|-----|
| 프론트엔드 (Vite dev) | http://localhost:3000 |
| 백엔드 | http://localhost:8080 |

> Vite dev 서버가 `/api` 와 `/ws` 를 `localhost:8080` 으로 프록시

### 테스트

```bash
# 백엔드 — 단위 + 통합 301개
./gradlew build

# 프론트엔드 — 단위 88개
cd frontend && npm test

# E2E — Playwright 18개 (스택이 떠 있어야 함)
docker compose up -d --build
cd frontend && npm run test:e2e
```

> 백엔드 테스트는 외부 인프라 없이 실행됩니다. `src/test/resources/application.yml` 이 인메모리 H2 를
> 사용하고 Flyway 를 꺼서 Hibernate 가 엔티티에서 스키마를 직접 만듭니다. 마이그레이션 검증만
> Testcontainers 로 실제 MySQL 을 띄우며, Docker 가 없으면 건너뜁니다.

> CI 는 백엔드 · 프론트엔드 · E2E 세 잡으로 나뉘어 있습니다.

</details>

---

<details>
<summary><b>Environment Variables</b> — .env 와 frontend/.env</summary>

**`.env` (루트)**: `.env.example` 복사 후 바로 사용 가능

```env
JWT_SECRET=collabus-dev-secret-key-for-local-testing-only-32chars
JWT_EXPIRATION=900000          # Access Token TTL (ms) — 기본 15분
JWT_REFRESH_EXPIRATION=604800000  # Refresh Token TTL (ms) — 기본 7일

DB_NAME=collabus
DB_USERNAME=collabus
DB_PASSWORD=collabus1234
DB_ROOT_PASSWORD=rootpassword1234

REDIS_HOST=localhost
REDIS_PORT=6379

# 로컬 실행 시 MySQL 접속 정보 (docker compose 는 mysql:3306 을 자동 주입)
DB_HOST=localhost
DB_PORT=3307
```

**`frontend/.env`**: `frontend/.env.example` 복사 후 바로 사용 가능

```env
VITE_API_BASE_URL=/api
VITE_WS_BASE_URL=/ws
```

</details>

---

## 핵심 구현

**WebSocket 인증** — 서버 → 클라이언트 알림 푸시 전용 단방향 채널입니다. 동시 편집은 다루지 않습니다.
- SockJS 핸드셰이크는 `Authorization` 헤더를 싣지 못합니다. `/ws/**` 를 HTTP 체인에서 열어두는 대신
  STOMP `CONNECT` 프레임에서 인증하는 `ChannelInterceptor` 를 직접 구현했습니다.
- 로그아웃 블랙리스트를 이 경로에서도 봅니다. HTTP 로는 끊긴 토큰으로 WebSocket 만 살아 있는 상태를 막습니다.
- 인증에 성공하면 `accessor.setUser` 로 principal 을 심어 `/user/{id}/queue/notifications` 라우팅이 동작합니다.
- 검증 갈래는 `WebSocketAuthInterceptorTest` 8개가 고정합니다.

**알림 발행은 커밋 이후**
- `@TransactionalEventListener(AFTER_COMMIT)` 으로 발행합니다. 롤백된 작업의 알림이 나가지 않습니다.

**Refresh Token Rotation**
- 재발급 시마다 새 RT 발급 + 기존 RT 즉시 무효화. Redis TTL 기반으로 만료 관리.

**Brute Force 방어**
- 로그인 5회 실패 시 Redis에 잠금 플래그 설정, 10분 TTL. DB 조회 없이 처리.

**비밀번호 변경 시 세션 강제 종료**
- 변경 즉시 해당 유저의 모든 RT Redis에서 삭제 → 타 기기 자동 로그아웃.

---

## Security

| 항목 | 구현 |
|------|------|
| 인증 | JWT Access Token (15분) + Refresh Token (7일) |
| Refresh Token Rotation | 재발급 시마다 새 RT 발급, 기존 RT 무효화 |
| 로그아웃 | Access Token 블랙리스트 등록 (Redis) |
| Brute Force 방어 | 5회 실패 시 10분 계정 잠금 (Redis) |
| 비밀번호 정책 | 8자 이상, 영문 + 숫자 필수 |
| 비밀번호 변경 | 현재 비밀번호 확인 후 변경, 기존 RT 무효화 → 타 기기 세션 강제 종료 |
| WebSocket 인증 | STOMP CONNECT 프레임에서 JWT 검증 + 로그아웃 블랙리스트 대조 |
| 파일 접근 제어 | 업로드 · 목록 · 다운로드 모두 해당 Task 참여자만 허용 |
| 업로드 제한 | 확장자 허용 목록 + 파일당 10MB, 경로 탈출 차단 |

---

## 만들며 겪은 것

동작하는 데까지 만들어놓고, 테스트를 71개에서 301개로 늘리며 코드를 처음부터 다시 읽었다.
그 과정에서 Todo 수정이 아예 안 되고 있었다는 것, 로그인이 실패 사유를 전부 "없는 이메일"
로 뭉개고 있었다는 것, 계정 잠금이 남의 계정을 잠그는 데 쓰일 수 있다는 것을 찾았다.
통과하고 있던 E2E 하나는 사실 조용히 건너뛰는 중이었다.

이 프로젝트에서 가장 값이 컸던 부분이다.

- [시행착오](docs/trial-and-error.md) — 짐작으로 시작한 것이 여러 번 틀렸다. 무엇을 믿었고 무엇으로 갈렸는지
- [문제 해결](docs/problem-solving.md) — 겪은 장애와 버그를 문제 → 원인 → 해결. 감수하기로 한 한계도 함께
