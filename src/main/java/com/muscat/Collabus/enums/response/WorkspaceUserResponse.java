package com.muscat.Collabus.enums.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "워크스페이스 멤버 응답 코드")
public enum WorkspaceUserResponse implements BaseResponseEnum{
  USER_REMOVED("200", "워크스페이스에서 사용자를 제거하였습니다."),
  USER_ROLE_UPDATED("200", "워크스페이스에서 사용자의 역할이 변경되었습니다."),
  SELF_LEFT("200", "워크스페이스에서 나갔습니다."),
  USER_ALREADY_MEMBER("409", "이미 워크스페이스에 참여 중인 사용자입니다."),
  NOT_FOUND_NEXT_MASTER ("409", "이미 워크스페이스의 다음 참여자입니다.");

  private final String code;
  private final String message;

}
