package com.muscat.Collabus.enums.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "워크스페이스 응답 코드")
public enum WorkspaceResponse implements BaseResponseEnum{

  WORKSPACE_CREATED("201", "워크스페이스 생성이 완료되었습니다."),
  WORKSPACE_ALREADY_EXISTS("409", "이미 사용 중인 워크스페이스 이름입니다.");

  private final String code;
  private final String message;

}
