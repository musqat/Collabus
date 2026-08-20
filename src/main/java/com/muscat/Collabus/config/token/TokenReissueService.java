package com.muscat.Collabus.config.token;

/**
 * Refresh Token 으로 Access Token 을 재발급한다.
 */
public interface TokenReissueService {

  // 저장된 Refresh Token 과 맞으면 새 토큰 쌍을 발급하고 기존 것을 무효화한다
  TokenResponseDto reissue(String refreshToken);
}
