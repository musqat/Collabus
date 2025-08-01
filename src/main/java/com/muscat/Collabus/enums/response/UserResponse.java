package com.muscat.Collabus.enums.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "유저 응답 코드")
public enum UserResponse implements BaseResponseEnum {

  USER_CREATED("201", "회원가입이 완료되었습니다."),
  EMAIL_ALREADY_EXISTS("409", "이미 사용 중인 이메일입니다."),
  NICKNAME_ALREADY_EXISTS("409", "이미 사용 중인 닉네임입니다.");

  private final String code;
  private final String message;

}
