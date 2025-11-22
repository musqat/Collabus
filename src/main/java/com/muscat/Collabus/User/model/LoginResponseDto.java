package com.muscat.Collabus.User.model;

import com.muscat.Collabus.enums.role.SystemRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "LoginResponse", description = "로그인 응답 DTO")
public class LoginResponseDto {

  @Schema(description = "사용자 ID", example = "1")
  private Long id;

  @Schema(description = "사용자 이메일", example = "user@example.com")
  private String email;

  @Schema(description = "닉네임", example = "muscat")
  private String nickname;

  @Schema(description = "닉네임#태그 조합", example = "muscat#1234")
  private String displayName;

  @Schema(description = "권한", example = "USER")
  private SystemRole role;

  @Schema(description = "access token")
  private String accessToken;

  @Schema(description = "refresh token")
  private String refreshToken;
}
