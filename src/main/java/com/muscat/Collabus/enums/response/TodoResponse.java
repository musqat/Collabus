package com.muscat.Collabus.enums.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Todo 응답 코드")
public enum TodoResponse implements BaseResponseEnum {
  ONLY_MANAGER_AUTHORIZED("403", "Todo에 대한 권한이 없습니다."),
  TODO_CREATED("201", "Todo 생성이 완료되었습니다."),
  TODO_ALREADY_ASSIGNED("409", "이미 참여 중인 Todo입니다."),
  TODO_WORK_NOT_FOUND("404", "해당 작업 기록을 찾을 수 없습니다."),
  UNAUTHORIZED_TODO_WORK("403", "작성자만 수정 또는 삭제할 수 있습니다."),
  FILE_NOT_FOUND("404", "해당 파일을 찾을 수 없습니다."),
  COMMENT_NOT_FOUND("404", "해당 코멘트를 찾을 수 없습니다."),
  NEED_BEFORE_TASK_DUE_DATE("400", "Todo의 마감일은 Task의 마감일보다 이전으로 설정해야합니다."),
  NEED_WAITING_REVIEW_STATUS("400", "WAITING_REVIEW 때만 완료할 수 있습니다."),
  ONLY_ASSIGNEE_CAN_COMPLETE("400", "Todo를 담당자만 완료처리를 할 수 있습니다,."),
  NEED_AFTER_NOW_DATE("400", "마감일은 현재 시간 이후로 설정 해야 합니다."),
  ALREADY_ASSIGNED_TO_USER("409", "이미 해당 사용자에게 할당되어 있습니다."),
  CANNOT_WORK_IN_CONFIRMED("400","Todo가 CONFIRMED상태면 작업을 만들 수 없습니다." );

  private final String code;
  private final String message;
}
