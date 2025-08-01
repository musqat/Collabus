package com.muscat.Collabus.config.token;

import java.util.Optional;

public interface RefreshTokenService {

  void saveRefreshToken(String email, String token, long ttlMillis);

  Optional<String> getRefreshToken(String email);

  void deleteRefreshToken(String email);

  void blacklistAccessToken(String token, long expirationMillis);

  boolean isBlacklisted(String token);

}
