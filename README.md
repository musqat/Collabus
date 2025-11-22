# Collabus

팀 협업 및 프로젝트 관리 플랫폼

## 프로젝트 개요

Workspace 내에서 Task와 Todo를 계층적으로 관리하고, 실시간 알림과 파일 공유를 통해 팀 협업을 지원하는 풀스택 웹 애플리케이션입니다.

## 주요 기능

- **워크스페이스 관리**: 워크스페이스 생성, 멤버 초대, 역할 기반 권한 관리
- **Task/Todo 관리**: 계층적 작업 구조, 담당자 할당, 진행률 시각화
- **협업 기능**: 댓글, 파일 첨부, 작업 내역 추적
- **실시간 알림**: WebSocket 기반 알림 (초대, 할당, 검수 요청, 댓글)
- **권한 체계**: Workspace/Task 단위 역할 기반 접근 제어 (MASTER, MANAGER, MEMBER)

## 기술 스택

**Backend**
- Java 21, Spring Boot 3.x
- Spring Security (JWT), Spring WebSocket (STOMP)
- Spring Data JPA, H2 Database, Redis

**Frontend**
- React 18, Vite, TanStack Query, Zustand
- Tailwind CSS, Recharts, SockJS/STOMP

**DevOps**
- Docker Compose, Nginx

## 핵심 구현

- JWT 기반 인증/인가 (Access + Refresh Token)
- WebSocket 실시간 양방향 통신
- 복합키를 활용한 다대다 관계 모델링
- RESTful API 설계 및 Swagger 문서화
- 역할 기반 접근 제어 (RBAC)

**Built with Spring Boot & React**
