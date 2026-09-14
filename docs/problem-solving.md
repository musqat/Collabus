# 문제 해결

겪은 버그를 원인과 해결만 짧게 적는다. 짐작이 틀린 이야기는 [시행착오](trial-and-error.md) 에 있다.

<br>

## Todo 수정이 아예 안 됐다

**원인** 둘이 겹쳤다. 마감일 입력이 `datetime-local` 인데 서버는 `LocalDate` 라 값이 안 채워져
`required` 에 막혔다. 그걸 고쳐도 수정 요청이 생성용 DTO 를 재사용해 `@NotNull taskId` 로 400 이었다.

**해결** 입력을 `date` 로 맞추고 `TodoUpdateRequestDto` 를 따로 만들었다. 하나만 고치면
증상이 "안 눌린다" 에서 "400" 으로 바뀔 뿐이다.

## 한글 85자가 넘는 댓글이 저장되지 않았다

**원인** 작업 내용·댓글의 `@Lob` 이 MySQL 에서 `tinytext`(255바이트)로 매핑됐다. UTF-8 한글은 3바이트라
85자를 넘기면 `Data too long for column 'content'` 로 실패했다.

**해결** `@Column(columnDefinition = "TEXT")` 로 바꾸고 Flyway V1 스키마도 TEXT 로 잡았다.

## 로그인 실패가 전부 "없는 이메일" 로 나갔다

**원인** 컨트롤러가 `BusinessException` 을 종류 구분 없이 잡아 무조건 "이메일 없음" 으로 응답하고
실패 횟수를 올렸다. 없는 이메일로도 잠금 카운트가 올랐다.

**해결** 응답 enum 으로 갈라 비밀번호 오류일 때만 센다. `UserControllerLoginTest` 7개가 갈래를 고정한다.

## 인증 실패가 403 으로 나가 재발급을 못 탔다

**원인** 인증이 없는 요청에 Spring Security 기본 진입점이 403 을 줬다. 프론트 인터셉터는 401 에만 재발급을
걸어서 이 경우 재발급을 시도하지 않았다. 필터 단계 응답은 `@RestControllerAdvice` 를 타지 않아 본문도 비어 있었다.

**해결** `AuthenticationEntryPoint` 로 인증 없음은 401, `AccessDeniedHandler` 로 권한 부족은 403 으로 나누고
필터 단계 응답도 컨트롤러 예외와 같은 형식으로 맞췄다.

## 동시에 401 을 받으면 무작위로 로그아웃됐다

**원인** 재발급 요청을 묶는 장치가 없었다. 여러 요청이 동시에 401 을 받으면 재발급이 중복으로 실행돼
무작위로 로그아웃됐다.

**해결** `isRefreshing` 플래그와 대기 큐로 재발급을 한 번만 하고 기다리던 요청은 새 토큰으로 재시도한다.
재시도된 응답은 응답 인터셉터를 다시 타므로 `statusCode` 가 있는 본문만 unwrap 해 두 번 벗겨지지 않게 했다.
