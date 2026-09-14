# 시행착오

"이럴 것이다" 하고 시작했다가 틀린 것들. 믿었던 것과 갈린 지점만 짧게 남긴다.

<br>

## 권한 확인은 조회일 뿐이라 부작용이 없을 줄 알았다

워크스페이스 삭제에 `ON DELETE CASCADE` 를 건 뒤, 삭제 요청이 예외 없이 끝나는데 DELETE 쿼리가
나가지 않았다. 권한 확인이 역할 하나를 보려고 `WorkspaceUser` 엔티티를 읽어 영속성 컨텍스트에
올린 게 원인이었다. 이 엔티티는 `@MapsId` 로 식별자를 `Workspace` 에서 가져온다.

→ 권한 확인을 `exists` 조회로 바꾸자 DELETE 가 나갔다. 권한 판정을 전부 엔티티 로드 없이 `exists` 로 통일했다.

## Refresh Token Rotation 을 넣었으니 동작하는 줄 알았다

JWT 에 고유 식별자가 없어 토큰이 `iat`·`exp` 의 초 단위로만 달랐다. 같은 초에 발급한 토큰은 문자열까지
같아서 재발급해도 이전 토큰이 그대로 유효했다.

→ 토큰마다 UUID `jti` 를 넣고 같은 초에 발급한 두 토큰이 다른지 `JwtUtilTest` 로 확인한다.

## 정렬은 순서만 바꾸니 민감하지 않을 줄 알았다

`Pageable` 이 클라이언트가 준 속성 경로를 그대로 받아서 `?sort=inviter.password` 가 users 를 조인해
비밀번호 순으로 정렬했다. 값은 응답에 안 나오지만 순서가 값을 좁히는 단서가 된다. 없는 속성을 주면 500 이었다.

→ `SortGuard` 가 엔티티 메타모델에 매핑된 속성만 남기고 나머지는 버린다. MySQL general_log 로
password 정렬 요청에 조인이 나가지 않는 것을 확인했다. 페이지 크기 상한은 100 으로 잡았다.

## 계정 잠금이 방어인 줄만 알았다

5회 실패 잠금은 **아무나 걸 수 있다.** 게다가 처음엔 없는 이메일로 틀려도 카운트가 올라서
아직 가입하지 않은 이메일까지 미리 잠가둘 수 있었다.

→ 비밀번호 오류일 때만 세도록 응답 enum 으로 갈랐다.

## 캐스케이드를 넣으면 삭제가 깔끔해질 줄 알았다

워크스페이스·Task·Todo 를 지우면 `todo_files` 레코드는 사라지는데 디스크의 실제 파일은 남았다.
캐스케이드 전에는 워크스페이스 삭제 자체가 실패해 파일도 그대로였지만 넣은 뒤로는 레코드만
정리되고 파일이 주인 없이 남았다.

→ 삭제 직전에 파일 경로를 모으고 실제 파일 삭제는 `FilesDeletedEvent` 를 `AFTER_COMMIT` 에서 처리한다.
권한 실패로 롤백되면 파일도 그대로 남는다.

## 통과하던 E2E 가 사실은 건너뛰고 있었다

페이지네이션 E2E 는 워크스페이스가 한 페이지를 못 채우면 `test.skip()` 했다. 앞선 테스트가
남긴 데이터 덕에 늘 통과했는데, 데모 데이터를 정리하자 조용히 건너뛰기 시작했다.
통과 로그로는 둘이 구분되지 않는다.

→ 검사에 필요한 워크스페이스 20개를 검사가 직접 만들고 `test.skip()` 을 뺐다.

## 전역 스위치가 대상을 고를 거라 봤다

Swagger 에서 `Pageable` 을 펼치려고 `springdoc.default-flat-param-object: true` 를 켰더니
`@AuthenticationPrincipal CustomUserDetails` 까지 펼쳐져 `password` 가 쿼리 파라미터처럼
문서에 나왔다.

→ 되돌리고 `@ParameterObject` 를 12곳에 직접 붙였다.

## 테스트를 늘리면 분기 커버리지도 따라 오를 줄 알았다

테스트 141개 시점에 분기 14.8%, 라인 50.1% 였다. 301개로 늘리자 라인은 72.6% 까지 올랐는데
분기는 33.5% 에 머물렀다. 남은 미커버 분기 상위가 Lombok 이 만든 `equals`/`hashCode`
(`LoginResponseDto` 62개, `InviteResponseDto` 53개)였다.

→ 채워도 검증되는 게 없어 덮지 않았다. 대신 코드를 일부러 깨뜨려 테스트가 잡아내는지로 확인했다.

## 로컬에서 통과하니 CI 도 통과할 줄 알았다

`jsdom@30` 을 넣고 로컬(Node 24)에서 전부 통과해 커밋했는데, CI(Node 20)가
`webidl.util.markAsUncloneable is not a function` 으로 죽었다. jsdom 30 이 끌고 오는
undici 8 이 Node 22 이상을 요구했다.

→ jsdom 을 26 으로 내려 급한 불을 끄고 CI 와 Dockerfile 을 Node 22 로 올렸다.
`engines`(`>=22.22.2`)와 `engine-strict` 로 버전이 낮으면 **설치 단계에서** 막힌다.

## MySQL 호환 DB 면 설정만 바꾸면 붙을 줄 알았다

무료 배포처로 TiDB 를 보며 로컬에 띄워봤다. Flyway V1~V5 는 그대로 돌았고 V4 의
`ON DELETE CASCADE` 도 실제로 지워보니(워크스페이스 1개 → Task 3개·Todo 30개) 동작했다.
그런데 앱이 안 떴다. Hibernate 가 dialect 를 판별하려고 읽는 `information_schema.KEYWORDS`
가 TiDB 에 없었다.

→ `spring.jpa.database-platform` 을 명시하니 `ddl-auto: validate` 까지 통과했다.

<br>

겪은 버그는 [문제 해결](problem-solving.md) 에 있다.
