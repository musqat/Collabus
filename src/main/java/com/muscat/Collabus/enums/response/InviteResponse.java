package com.muscat.Collabus.enums.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "초대 응답 코드")
public enum InviteResponse implements BaseResponseEnum{

  INVITE_ALREADY_PENDING("409", "이미 대기 중인 초대가 존재합니다."),
  INVITE_ALREADY_PROCESSED("409", "이미 처리된 초대 입니다.."),
  INVITE_NOT_FOUND("404", "해당 초대를 찾을 수 없습니다."),
  INVITE_SELF("400", "자기 자신을 초대할 수 없습니다.");

  private final String code;
  private final String message;

}
