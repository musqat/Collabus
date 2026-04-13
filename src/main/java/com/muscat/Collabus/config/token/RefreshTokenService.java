package com.muscat.Collabus.config.token;

import java.util.Optional;

public interface RefreshTokenService {

  void saveRefreshToken(String email, String token, long ttlMillis);

  Optional<String> getRefreshToken(String email);

  void deleteRefreshToken(String email);

  void blacklistAccessToken(String token, long expirationMillis);

  boolean isBlacklisted(String token);

  // 로그인 실패 횟수 증가 (반환값: 누적 실패 횟수)
  int incrementLoginFailure(String email);

  // 로그인 성공 시 실패 횟수 초기화
  void resetLoginFailure(String email);

  // 계정 잠금 여부 확인 (5회 실패 시 10분 잠금)
  boolean isAccountLocked(String email);
}
