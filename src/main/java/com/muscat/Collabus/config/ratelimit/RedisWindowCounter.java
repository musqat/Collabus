package com.muscat.Collabus.config.ratelimit;

import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

/**
 * 일정 시간 창 안의 횟수를 Redis 에 센다. 증가와 만료 설정을 한 번에 처리한다.
 */
@Component
@RequiredArgsConstructor
public class RedisWindowCounter {

  // 증가 후 만료가 없으면 창 길이만큼 만료 설정
  private static final RedisScript<Long> INCREMENT_WITH_EXPIRE = new DefaultRedisScript<>(
      "local count = redis.call('INCR', KEYS[1]) "
          + "if redis.call('PTTL', KEYS[1]) < 0 then "
          + "redis.call('PEXPIRE', KEYS[1], ARGV[1]) "
          + "end "
          + "return count",
      Long.class);

  private final StringRedisTemplate redisTemplate;

  // 1 올린 값
  public long increment(String key, long windowMillis) {
    Long count = redisTemplate.execute(
        INCREMENT_WITH_EXPIRE, List.of(key), String.valueOf(windowMillis));
    return count == null ? 1 : count;
  }

  // 창이 끝날 때까지 남은 밀리초, 키가 없거나 만료가 없으면 0
  public long remainingMillis(String key) {
    Long ttl = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
    return ttl == null || ttl < 0 ? 0 : ttl;
  }
}
