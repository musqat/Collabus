package com.muscat.Collabus.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "유저 공통 응답 코드")
public enum UserResponse {

  SUCCESS("200", "요청이 정상적으로 처리되었습니다."),
  USER_CREATED("201", "회원가입이 완료되었습니다."),
  EMAIL_ALREADY_EXISTS("409", "이미 사용 중인 이메일입니다."),
  NICKNAME_ALREADY_EXISTS("410", "이미 사용 중인 닉네임입니다."),
  LOGIN_OK("200", "로그인에 성공하였습니다."),
  LOGIN_FAILED("401", "이메일 또는 비밀번호가 일치하지 않습니다."),
  UPDATE_FAILED("400", "회원 정보 수정에 실패했습니다."),
  DELETE_FAILED("400", "회원 삭제에 실패했습니다."),
  USER_NOT_FOUND("404", "사용자를 찾을 수 없습니다."),
  INTERNAL_SERVER_ERROR("500", "서버의 문제가 발생하였습니다.");

  private final String code;
  private final String message;

}
