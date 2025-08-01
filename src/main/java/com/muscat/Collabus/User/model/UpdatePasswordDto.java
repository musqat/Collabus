package com.muscat.Collabus.User.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "비밀번호 수정 요청 DTO")
public class UpdatePasswordDto {
  @Schema(description = "새 비밀번호", example = "newPassword123!")
  private String password;
}
