package com.muscat.Collabus.User.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(name = "Login", description = "로그인 정보 DTO")
public class LoginDto {

  @Email
  @NotBlank
  @Schema(description = "사용자 이메일", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
  private String email;

  @NotBlank
  @Schema(description = "사용자 비밀번호", example = "password", requiredMode = Schema.RequiredMode.REQUIRED)
  private String password;

}
