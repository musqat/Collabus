package com.muscat.Collabus.User.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "닉네임 수정 요청 DTO")
public class UpdateNicknameDto {
  @Schema(description = "변경할 닉네임", example = "가나다라 -> 라다나가")
  private String nickname;
}
