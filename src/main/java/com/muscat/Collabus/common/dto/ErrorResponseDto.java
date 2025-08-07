package com.muscat.Collabus.common.dto;

import com.muscat.Collabus.enums.response.ErrorType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@Schema(name = "ErrorResponse", description = "에러 응답 DTO")
public class ErrorResponseDto {

  @Schema(description = "요청 경로", example = "/api/tasks/1")
  private String path;

  @Schema(description = "HTTP 상태 코드", example = "400")
  private HttpStatus status;

  @Schema(description = "에러 메시지", example = "이미 등록된 사용자입니다.")
  private String message;

  @Schema(description = "에러 발생 시각", example = "2025-08-05T14:00:00")
  private LocalDateTime timestamp;

  @Schema(description = "에러 타입", example = "BUSINESS")
  private ErrorType errorType;
}
