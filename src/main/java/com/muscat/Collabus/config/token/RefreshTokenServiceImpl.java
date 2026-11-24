package com.muscat.Collabus.config.token;

import com.muscat.Collabus.config.ratelimit.RedisWindowCounter;
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
  private final RedisWindowCounter windowCounter;
  private static final String KEY_PREFIX = "RT:";
  private static final String BLACKLIST_PREFIX = "BL:";
  private static final String LOGIN_FAIL_PREFIX = "LF:";
  private static final int MAX_LOGIN_ATTEMPTS = 5;
  private static final long LOCK_DURATION_MILLIS = 10 * 60 * 1000L; // 10분

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

  @Override
  public int incrementLoginFailure(String email) {
    String key = LOGIN_FAIL_PREFIX + email;
    long count = windowCounter.increment(key, LOCK_DURATION_MILLIS);
    // 잠기는 실패에 잠금 시간 재설정
    if (count == MAX_LOGIN_ATTEMPTS) {
      redisTemplate.expire(key, LOCK_DURATION_MILLIS, TimeUnit.MILLISECONDS);
    }
    return (int) count;
  }

  @Override
  public void resetLoginFailure(String email) {
    redisTemplate.delete(LOGIN_FAIL_PREFIX + email);
  }

  @Override
  public boolean isAccountLocked(String email) {
    String key = LOGIN_FAIL_PREFIX + email;
    String value = redisTemplate.opsForValue().get(key);
    if (value == null) return false;
    return Integer.parseInt(value) >= MAX_LOGIN_ATTEMPTS;
  }
}
