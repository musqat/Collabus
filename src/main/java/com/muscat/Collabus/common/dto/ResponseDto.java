package com.muscat.Collabus.common.dto;

import com.muscat.Collabus.enums.response.BaseResponseEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(
    name = "Response",
    description = "공통 응답 DTO"
)
@Data
@AllArgsConstructor
public class ResponseDto {

  @Schema(description = "응답 코드", example = "200")
  private String statusCode;

  @Schema(description = "응답 메시지", example = "성공")
  private String statusMsg;

  @Schema(description = "응답 데이터")
  private Object data;

  public ResponseDto(BaseResponseEnum response) {
    this.statusCode = response.getCode();
    this.statusMsg = response.getMessage();
  }

  public ResponseDto(BaseResponseEnum response, Object data) {
    this(response);
    this.data = data;
  }


}
