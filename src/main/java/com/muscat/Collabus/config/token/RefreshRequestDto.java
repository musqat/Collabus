package com.muscat.Collabus.config.token;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(name = "RefreshRequest", description = "Refresh 토큰 재발급 요청 DTO")
public class RefreshRequestDto {

  @Schema(description = "refresh token", requiredMode = Schema.RequiredMode.REQUIRED)
  private String refreshToken;
}
