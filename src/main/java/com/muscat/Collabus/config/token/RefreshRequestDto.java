package com.muscat.Collabus.config.token;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(name = "RefreshRequest", description = "Refresh 토큰 재발급 요청 DTO")
public class RefreshRequestDto {

  @Schema(description = "refresh token", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "Refresh Token 은 필수입니다.")
  private String refreshToken;
}
