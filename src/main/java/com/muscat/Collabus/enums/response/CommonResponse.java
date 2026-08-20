package com.muscat.Collabus.enums.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "공통 응답 코드")
public enum CommonResponse implements BaseResponseEnum {

  // 성공
  SUCCESS("200", "요청이 정상적으로 처리되었습니다."),

  //  실패
  BAD_REQUEST("400", "잘못된 요청입니다."),
  UNAUTHORIZED("401", "인증이 필요합니다."),
  FORBIDDEN("403", "권한이 없습니다."),
  RESOURCE_NOT_FOUND("404", "요청한 리소스를 찾을 수 없습니다."),
  INTERNAL_SERVER_ERROR("500", "서버 내부 오류가 발생했습니다."),

  // CRUD  실패
  DELETE_FAILED("423", "삭제에 실패했습니다."),


  USER_NOT_FOUND("404", "해당 사용자가 존재하지 않습니다."),
  WORKSPACE_NOT_FOUND("404", "해당 Workspace가 존재하지 않습니다."),
  TASK_NOT_FOUND("404", "해당 Task가 존재하지 않습니다."),
  TASK_USER_NOT_FOUND("404", "해당 Task에 참여한 사용자가 존재하지 않습니다."),
  TODO_NOT_FOUND("404", "해당 Todo가 존재하지 않습니다."),



  CANNOT_REMOVE_SELF("412", "자기 자신은 제거할 수 없습니다."),
  CANNOT_CHANGE_SELF_ROLE("412", "자기 자신의 역할은 변경할 수 없습니다."),

  WORKSPACE_MASTER_REQUIRED("403", "Workspace Master 권한이 필요합니다."),
  WORKSPACE_PARTICIPANT_REQUIRED("403", "워크스페이스 참여자만 접근할 수 있습니다."),
  TASK_MANAGER_REQUIRED("403", "Task Manager 권한이 필요합니다."),
  TASK_MANAGE_DENIED("403", "Task 관리 권한이 없습니다."),
  TASK_CREATE_DENIED("403", "Task 생성 권한이 없습니다. MASTER 또는 MANAGER 만 가능합니다."),
  TASK_VIEW_DENIED("403", "Task 를 볼 권한이 없습니다."),
  TODO_VIEW_DENIED("403", "Todo 를 볼 권한이 없습니다."),
  TASK_PARTICIPANT_REQUIRED("403", "Task 참여자만 접근할 수 있습니다.");

  private final String code;
  private final String message;

}
