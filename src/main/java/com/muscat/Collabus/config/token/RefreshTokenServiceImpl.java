package com.muscat.Collabus.config.token;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

  private final StringRedisTemplate redisTemplate;
  private static final String KEY_PREFIX = "RT:";
  private static final String BLACKLIST_PREFIX = "BL:";

  @Override
  public void saveRefreshToken(String email, String token, long ttlMillis) {
    String key = KEY_PREFIX + email;
    ValueOperations<String, String> ops = redisTemplate.opsForValue();
    ops.set(key, token, ttlMillis, TimeUnit.MILLISECONDS);
  }

  @Override
  public Optional<String> getRefreshToken(String email) {
    String key = KEY_PREFIX + email;
    String token = redisTemplate.opsForValue().get(key);
    return Optional.ofNullable(token);
  }

  @Override
  public void deleteRefreshToken(String email) {
    String key = KEY_PREFIX + email;
    redisTemplate.delete(key);
  }

  @Override
  public void blacklistAccessToken(String token, long expirationMillis) {
    String key = BLACKLIST_PREFIX + token;
    redisTemplate.opsForValue().set(key, "logout", expirationMillis, TimeUnit.MILLISECONDS);
  }

  @Override
  public boolean isBlacklisted(String token) {
    String key = BLACKLIST_PREFIX + token;
    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
  }
}
