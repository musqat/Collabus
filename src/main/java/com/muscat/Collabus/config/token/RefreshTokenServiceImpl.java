package com.muscat.Collabus.config.token;

import com.muscat.Collabus.config.ratelimit.RedisWindowCounter;
import java.util.Optional;
import java.util.Set;
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
  private static final String SESSIONS_PREFIX = "RTS:";
  // 한 사용자가 동시에 유지하는 로그인 세션 상한. 넘으면 오래된 세션부터 정리
  static final int MAX_SESSIONS = 10;
  private static final String BLACKLIST_PREFIX = "BL:";
  private static final String LOGIN_FAIL_PREFIX = "LF:";
  private static final int MAX_LOGIN_ATTEMPTS = 5;
  private static final long LOCK_DURATION_MILLIS = 10 * 60 * 1000L; // 10분

  @Override
  public void saveRefreshToken(String email, String sessionId, String token, long ttlMillis) {
    ValueOperations<String, String> ops = redisTemplate.opsForValue();
    ops.set(tokenKey(email, sessionId), token, ttlMillis, TimeUnit.MILLISECONDS);

    // 세션 목록은 마지막 사용 시각 순. 목록 자체도 마지막 세션과 함께 만료
    String sessionsKey = SESSIONS_PREFIX + email;
    redisTemplate.opsForZSet().add(sessionsKey, sessionId, System.currentTimeMillis());
    redisTemplate.expire(sessionsKey, ttlMillis, TimeUnit.MILLISECONDS);
    evictOldestSessions(email, sessionsKey);
  }

  @Override
  public Optional<String> getRefreshToken(String email, String sessionId) {
    return Optional.ofNullable(redisTemplate.opsForValue().get(tokenKey(email, sessionId)));
  }

  @Override
  public void deleteRefreshToken(String email, String sessionId) {
    redisTemplate.delete(tokenKey(email, sessionId));
    redisTemplate.opsForZSet().remove(SESSIONS_PREFIX + email, sessionId);
  }

  @Override
  public void deleteAllRefreshTokens(String email) {
    String sessionsKey = SESSIONS_PREFIX + email;
    Set<String> sessionIds = redisTemplate.opsForZSet().range(sessionsKey, 0, -1);
    if (sessionIds != null) {
      sessionIds.forEach(sessionId -> redisTemplate.delete(tokenKey(email, sessionId)));
    }
    redisTemplate.delete(sessionsKey);
  }

  // 상한을 넘은 만큼 가장 오래 쓰지 않은 세션 삭제
  private void evictOldestSessions(String email, String sessionsKey) {
    Long size = redisTemplate.opsForZSet().zCard(sessionsKey);
    if (size == null || size <= MAX_SESSIONS) {
      return;
    }
    Set<String> oldest = redisTemplate.opsForZSet().range(sessionsKey, 0, size - MAX_SESSIONS - 1);
    if (oldest == null) {
      return;
    }
    oldest.forEach(sessionId -> deleteRefreshToken(email, sessionId));
  }

  private static String tokenKey(String email, String sessionId) {
    return KEY_PREFIX + email + ":" + sessionId;
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
