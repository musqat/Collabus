package com.muscat.Collabus.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@Schema(
    name = "ErrorResponse",
    description = "에러 응답 정보를 담는 객체"
)
public class ErrorResponseDto {

  @Schema(
      description = "클라이언트가 호출한 API 경로",
      example = "/api/users/login"
  )
  private String apiPath;

  @Schema(
      description = "HTTP 상태 코드",
      example = "BAD_REQUEST"
  )
  private HttpStatus errorCode;

  @Schema(
      description = "에러 상세 메시지",
      example = "비밀번호가 일치하지 않습니다."
  )
  private String errorMessage;

  @Schema(
      description = "에러가 발생한 시간",
      example = "YYYY-MM-DDT00:00:00"
  )
  private LocalDateTime errorTime;
}
