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
  RESOURCE_ALREADY_EXISTS("409", "이미 존재하는 리소스입니다."),
  INTERNAL_SERVER_ERROR("500", "서버 내부 오류가 발생했습니다."),

  // CRUD  실패
  CREATE_FAILED("420", "생성에 실패했습니다."),
  READ_FAILED("421", "조회에 실패했습니다."),
  UPDATE_FAILED("422", "수정에 실패했습니다."),
  DELETE_FAILED("423", "삭제에 실패했습니다.");

  private final String code;
  private final String message;

}
