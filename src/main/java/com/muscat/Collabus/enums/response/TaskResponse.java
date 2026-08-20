package com.muscat.Collabus.enums.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Task 응답 코드")
public enum TaskResponse implements BaseResponseEnum {

  TASK_NOT_FOUND("404", "해당 Task가 존재하지 않습니다."),
  TASK_USER_NOT_FOUND("405", "해당 Task에 참여한 유저가 존재하지 않습니다."),
  TASK_USER_ALREADY_EXISTS("409", "이미 Task에 참여 중인 유저입니다.");

  private final String code;
  private final String message;
}
