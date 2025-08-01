package com.muscat.Collabus.config.token;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(name = "TokenResponse", description = "Access/Refresh 토큰 응답 DTO")
public class TokenResponseDto {

  @Schema(description = "access token")
  private String accessToken;

  @Schema(description = "refresh token")
  private String refreshToken;
}
