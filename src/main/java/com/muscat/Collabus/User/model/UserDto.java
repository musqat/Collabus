package com.muscat.Collabus.User.model;

import com.muscat.Collabus.enums.SystemRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "User", description = "사용자 정보를 담는 DTO")
public class UserDto {

  @Schema(description = "사용자 고유 ID", example = "1")
  private Long id;

  @Schema(description = "사용자 이메일", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
  @Email
  @NotBlank
  private String email;

  @Schema(description = "사용자 닉네임", example = "user", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank
  private String nickname;

  @Schema(description = "사용자 비밀번호", example = "password", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank
  private String password;

  @Schema(description = "사용자 권한 (예: USER, ADMIN)", example = "USER")
  private SystemRole role;

  @Schema(description = "사용자 표시 이름", example = "user#1234")
  private String displayName;
}
