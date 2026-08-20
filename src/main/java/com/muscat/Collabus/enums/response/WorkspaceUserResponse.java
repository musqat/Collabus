package com.muscat.Collabus.enums.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "워크스페이스 멤버 응답 코드")
public enum WorkspaceUserResponse implements BaseResponseEnum{
  USER_ALREADY_MEMBER("409", "이미 워크스페이스에 참여 중인 사용자입니다."),
  NOT_FOUND_NEXT_MASTER ("409", "이미 워크스페이스의 다음 참여자입니다.");

  private final String code;
  private final String message;

}
