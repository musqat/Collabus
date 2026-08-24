# 문제 해결

만들고 점검하며 겪은 장애와 버그를 문제 → 원인 → 해결 순으로 적는다.
가정이 틀렸던 이야기는 [시행착오](trial-and-error.md) 에 따로 있다.

- [Todo 수정이 아예 안 됐다](#todo-수정이-아예-안-됐다) — 원인이 두 개였다
- [로그인 실패가 전부 "없는 이메일" 로 보였다](#로그인-실패가-전부-없는-이메일-로-보였다) — 예외를 한 덩어리로 잡음
- [읽기와 쓰기 권한이 어긋나 있었다](#읽기와-쓰기-권한이-어긋나-있었다) — 볼 수 있는데 못 씀
- [`data.data` 두 겹](#datadata-두-겹) — 인터셉터가 두 번 도는 문제
- [서버가 준 실패 사유가 절반 사라졌다](#서버가-준-실패-사유가-절반-사라졌다) — `message` 와 `statusMsg`
- [Swagger 에 Authorize 버튼이 없었다](#swagger-에-authorize-버튼이-없었다) — 전부 401
- [알고 두는 한계](#알고-두는-한계)

<br>

## Todo 수정이 아예 안 됐다

`문제`
Todo 수정 폼에서 저장이 되지 않았다. 브라우저가 제출 자체를 막는 경우와, 제출은 되는데
서버가 400 을 주는 경우가 섞여 있었다. **원인이 하나가 아니라 두 개였다.**

`원인 1 — 프론트`
마감일 입력이 `datetime-local` 인데 서버는 `LocalDate`(`yyyy-MM-dd`) 를 준다. 형식이
맞지 않아 값이 채워지지 않았고, `required` 에 걸려 제출 버튼이 아무 반응도 하지 않았다.
콘솔에 오류가 남지 않아 "버튼이 안 눌린다" 로만 보였다.

`원인 2 — 백엔드`
입력 형식을 `date` 로 맞춰 제출이 통과해도 `PATCH /api/todo/{id}` 가 400 이었다.
수정 요청이 **생성용 DTO 를 그대로 재사용**하고 있었는데, 거기엔 `taskId` 가
`@NotNull` 로 박혀 있었다. 수정에서는 Todo 가 속한 Task 를 바꾸지 않으므로 화면이
보낼 값이 아니다. 보낼 이유가 없는 값을 필수로 요구하니 항상 검증에서 걸렸다.

`해결`
입력 형식을 `date` 로 맞추고, 수정 전용 DTO 를 따로 만들었다.

```java
public class TodoUpdateRequestDto {
  @NotBlank private String title;
  @Size(max = 2000) private String description;
  @NotNull private LocalDate dueDate;
}
```

생성과 수정은 필수 항목이 다르다. DTO 를 재사용하면 코드는 줄지만 **한쪽의 제약이
다른 쪽을 막는다.**

앞의 원인 하나만 고치면 증상이 "안 눌린다" 에서 "400 이 난다" 로 바뀔 뿐 여전히 안 된다.
증상이 바뀐 것을 진전으로 보고 멈추지 않은 게 이 건에서 중요했다.

<br>

## 로그인 실패가 전부 "없는 이메일" 로 보였다

`문제`
비밀번호를 틀리게 넣어도 화면에 "등록되지 않은 이메일" 이 떴다. 그리고 존재하지 않는
이메일로 시도해도 계정 잠금 카운트가 올라갔다.

`원인`
로그인 컨트롤러가 서비스에서 올라오는 `BusinessException` 을 종류 구분 없이 한 덩어리로
잡고 있었다. 잡은 뒤에는 무조건 "이메일 없음" 으로 응답하고, 무조건 실패 횟수를 올렸다.
비밀번호 오류와 이메일 없음이 같은 예외 타입으로 올라오는데 그걸 가르지 않은 것이다.

실패 횟수가 이메일 단위로 쌓이므로, 없는 이메일까지 세면 **아무 문자열이나 다섯 번 넣어
그 이메일이 나중에 가입될 때까지 잠가둘 수 있다.**

`해결`
예외가 들고 있는 응답 enum 으로 갈랐다.

```java
} catch (BusinessException e) {
    // 비밀번호가 틀린 경우에만 실패 횟수를 올린다.
    // 없는 이메일까지 세면 남의 계정을 잠글 수 있다
    if (e.getResponse() != INVALID_PASSWORD) {
        return ResponseEntity.status(NOT_FOUND).body(new ResponseDto(EMAIL_NOT_FOUND));
    }
    int failures = refreshTokenService.incrementLoginFailure(email);
    ...
}
```

이 갈래를 `UserControllerLoginTest` 7개가 고정한다. 실재하는 계정에 대한 잠금 공격은
여전히 열려 있는데, 이유는 [알고 두는 한계](#알고-두는-한계) 에 적었다.

<br>

## 읽기와 쓰기 권한이 어긋나 있었다

`문제`
워크스페이스 MASTER 가 자기가 참여하지 않은 Task 의 댓글을 **읽을 수는 있는데 쓸 수는
없었다.** 화면에는 입력창이 보이는데 보내면 거부된다.

`원인`
조회는 `validateCanViewTask`, 작성은 `validateTaskParticipant` 를 타고 있었다. 두 검사의
기준이 달랐다. MASTER 는 워크스페이스의 모든 Task 를 볼 수 있지만 참여자는 아니다.
권한을 검사하는 곳이 늘어나는 동안 기준이 갈라진 것을 아무도 잡지 못했다.

`해결`
작성 검사를 조회 검사로 바꿔 **"볼 수 있으면 쓸 수 있다"** 로 맞췄다. 수정·삭제는
그대로 작성자 본인만 가능하다.

판정 구현도 정리했다. `getWorkspaceRole` 이 `WorkspaceUser` 를 통째로 읽어 역할을
비교하고 있었는데 필요한 건 불리언 하나였다. `isWorkspaceMaster(Task, ..)` 는 기존
`Workspace` 오버로드로, `canCreateTask` 는 조건이 같은 `canViewAllTasks` 로 위임해
전부 `exists` 로 통일했다. 쓰이지 않게 된 `getWorkspaceRole` 과
`findById_WorkspaceIdAndId_UserId` 는 지웠다.

이 경계는 `TaskPermissionIntegrationTest` 29개가 고정한다. 역할 × 동작 조합을
`@CsvSource` 표로 돌리고, 상태 코드만 보지 않고 실제로 저장됐는지까지 확인한다.
권한 기준이 다시 갈라지면 표가 먼저 깨진다.

<br>

## `data.data` 두 겹

`문제`
API 호출부가 전부 이렇게 생겼다.

```js
const res = await apiClient.get('/api/tasks');
const list = res.data.data.content;
```

`원인`
axios 의 응답 본문도 `data` 고, 백엔드 `ResponseDto` 의 payload 필드도 `data` 다.
이름이 겹치면서 호출부 33곳이 두 겹으로 벗기고 있었다. 그중 `WorkspaceController` 만
`ResponseDto` 를 쓰지 않고 DTO 를 직접 반환해서, **이 엔드포인트만 한 겹**이었다.
E2E fixture 도 워크스페이스 생성 응답에서만 `.data` 를 거치지 않고 있었다.

`해결`
먼저 `WorkspaceController` 를 다른 10개 컨트롤러와 같은 형태로 맞췄다. 그다음 응답
인터셉터에서 한 겹을 벗겼다.

```js
// ResponseDto 를 unwrap 한다.
// axios 의 응답 본문과 ResponseDto 의 payload 가 둘 다 data 라 호출부가 data.data 를 써야 했다.
// 재발급 후 재시도된 응답이 이 체인을 다시 타므로, ResponseDto 일 때만 unwrap 해 두 번 하지 않는다.
apiClient.interceptors.response.use((response) => {
  const body = response.data;
  if (body && typeof body === 'object' && 'statusCode' in body) {
    response.data = body.data;
  }
  return response;
});
```

주석의 마지막 줄이 이 코드의 함정이다. 401 이 나면 토큰을 재발급하고 원래 요청을
재시도하는데, **재시도된 응답이 인터셉터 체인을 다시 탄다.** 조건 없이 벗기면 두 번
벗겨져서 재발급 직후의 요청만 데이터가 사라진다. 재현 조건이 "토큰이 만료된 뒤 첫 요청"
이라 눈으로 잡기 어렵다.

`statusCode` 검사를 일부러 빼고 테스트를 돌려 이 경로가 실제로 잡히는지 확인한 뒤
되돌렸다.

<br>

## 서버가 준 실패 사유가 절반 사라졌다

`문제`
어떤 화면은 서버가 준 구체적인 실패 사유("이미 초대된 사용자입니다")를 보여주는데,
어떤 화면은 같은 상황에서 "요청에 실패했습니다" 같은 기본 문구만 떴다.

`원인`
백엔드가 실패 사유를 **두 가지 필드로** 내려주고 있었다.

```
GlobalExceptionHandler 가 만든 응답   → message
컨트롤러가 직접 만든 응답             → statusMsg
```

프론트는 파일마다 둘 중 한쪽만 읽었다. 자기가 읽는 필드가 없는 응답을 만나면 조용히
기본 문구로 떨어진다. 오류가 나지 않으니 화면을 하나하나 보기 전에는 티가 안 났다.

`해결`
백엔드 응답 형태를 지금 바꾸면 영향 범위가 커서, 읽는 쪽을 헬퍼 하나로 모았다.
`errorMessage` 가 두 필드를 모두 보고, 없으면 호출부가 준 기본 문구를 쓴다.
`alert()` 44곳을 토스트로 옮기면서 이 헬퍼를 통과하게 했다.

<br>

## Swagger 에 Authorize 버튼이 없었다

`문제`
API 문서에서 Try it out 을 누르면 인증이 필요한 모든 엔드포인트가 401 이었다. 토큰을
넣을 자리가 화면에 없었다.

`원인`
springdoc 이 엔드포인트는 잘 훑어 왔지만 `securitySchemes` 가 정의된 적이 없었다.
스킴이 없으면 Swagger UI 는 Authorize 버튼 자체를 그리지 않는다. 문서는 있는데
**써볼 수는 없는 상태**로 계속 있었다.

`해결`
Bearer 스킴을 등록하고 전역 보안 요구사항으로 걸었다.

```java
SecurityScheme bearer = new SecurityScheme()
    .type(SecurityScheme.Type.HTTP)
    .scheme("bearer").bearerFormat("JWT")
    .description("로그인 응답의 accessToken 을 넣는다. Bearer 접두사는 붙이지 않는다.");
```

접두사 안내를 설명에 넣은 건, `Bearer ` 를 직접 붙여 넣어 실패하는 경우가 흔해서다.

같이 걸린 것이 `Pageable` 이다. 12곳에서 객체 하나가 통째로 필수 파라미터로 잡혀
페이지 값을 넣을 수 없었다. `@ParameterObject` 로 `page`·`size`·`sort` 로 펼쳤다.
이걸 전역 스위치로 해결하려다 벌어진 일은
[시행착오](trial-and-error.md#전역-스위치가-대상을-고를-거라-봤다) 에 있다.

<br>

## 알고 두는 한계

고칠 수 있는데 안 고친 것이 아니라, **비용과 범위를 보고 지금은 감수하기로 한 것**들이다.

**토큰을 localStorage 에 저장한다.**
XSS 가 발생하면 토큰이 그대로 노출된다. HttpOnly 쿠키가 더 안전하지만 CSRF 대응과
쿠키 기반 재발급 흐름을 함께 만들어야 한다. 대신 Access Token 을 15분으로 짧게 잡고
로그아웃 시 블랙리스트에 올려 노출 창을 줄였다.

**비밀번호를 바꿔도 이미 발급된 Access Token 은 최대 15분 유효하다.**
Refresh Token 은 즉시 전부 지워서 재발급은 막힌다. 발급된 AT 까지 즉시 끊으려면
사용자별 토큰 버전을 두고 검증마다 대조해야 한다.

**계정 잠금은 서비스 거부에 열려 있다.**
남의 이메일을 알면 아무 비밀번호나 다섯 번 넣어 그 계정을 10분간 잠글 수 있다.
없는 이메일로 카운트가 오르는 것은 막았지만, 실재하는 계정에 대한 잠금은 그대로다.
계정 잠금 방식 자체의 성질이라 지연 증가·CAPTCHA·IP 단위 제한을 얹어야 균형이 잡힌다.

**파일은 로컬 디스크에 저장한다.**
`uploads` 디렉터리(도커에서는 named volume)에 둔다. 단일 인스턴스 전제이며 다중
인스턴스로 늘리려면 오브젝트 스토리지로 옮겨야 한다.

**알림 발행은 통합 테스트에서 돌지 않는다.**
`NotificationEventListener` 가 `@TransactionalEventListener(AFTER_COMMIT)` 인데 통합
테스트는 트랜잭션을 롤백한다. 커밋이 없으니 리스너가 호출되지 않는다. 확인하려면
`@Transactional` 없이 수동 정리하는 테스트가 따로 필요하다.

**Flyway 검증은 Docker 가 있어야 돈다.**
없으면 `@EnabledIf` 로 건너뛴다. 로컬 Docker Desktop 이 Testcontainers 요청을 거부하는
경우가 있어 실질적으로 CI 에서만 검증된다.
