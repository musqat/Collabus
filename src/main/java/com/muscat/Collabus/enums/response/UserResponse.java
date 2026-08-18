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
  NICKNAME_ALREADY_EXISTS("409", "이미 사용 중인 닉네임입니다."),
  PASSWORD_BLANK("400", "비밀번호를 입력해주세요."),
  EMAIL_NOT_FOUND("404", "해당 이메일이 존재하지 않습니다."),
  INVALID_PASSWORD("401", "비밀번호가 일치하지 않습니다."),
  CURRENT_PASSWORD_MISMATCH("400", "현재 비밀번호가 일치하지 않습니다."),
  LOGIN_ATTEMPTS_EXCEEDED("429", "로그인 시도 횟수를 초과했습니다. 10분 후 다시 시도해주세요.");

  private final String code;
  private final String message;

}
