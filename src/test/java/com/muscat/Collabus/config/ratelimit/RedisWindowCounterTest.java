package com.muscat.Collabus.config.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Lua 스크립트는 대역으로 확인할 수 없어 실제 Redis 에 돌린다.
 */
@Testcontainers
@EnabledIf(value = "dockerAvailable", disabledReason = "Docker 를 사용할 수 없는 환경에서는 건너뛴다")
@DisplayName("Redis 시간 창 카운터")
class RedisWindowCounterTest {

  private static final String KEY = "RL:test";
  private static final long WINDOW = 60_000L;

  @Container
  static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

  private static LettuceConnectionFactory connectionFactory;
  private static StringRedisTemplate redisTemplate;
  private static RedisWindowCounter counter;

  static boolean dockerAvailable() {
    try {
      return DockerClientFactory.instance().isDockerAvailable();
    } catch (Throwable e) {
      return false;
    }
  }

  @BeforeAll
  static void connect() {
    connectionFactory = new LettuceConnectionFactory(
        new RedisStandaloneConfiguration(redis.getHost(), redis.getMappedPort(6379)));
    connectionFactory.afterPropertiesSet();
    connectionFactory.start();
    redisTemplate = new StringRedisTemplate(connectionFactory);
    counter = new RedisWindowCounter(redisTemplate);
  }

  @AfterAll
  static void disconnect() {
    connectionFactory.destroy();
  }

  @BeforeEach
  void clear() {
    redisTemplate.delete(KEY);
  }

  @Test
  @DisplayName("첫 증가에 창 길이만큼 만료를 건다")
  void firstIncrementSetsExpire() {
    assertThat(counter.increment(KEY, WINDOW)).isEqualTo(1);

    assertThat(counter.remainingMillis(KEY)).isBetween(WINDOW - 1_000, WINDOW);
  }

  @Test
  @DisplayName("이어진 증가는 만료를 늘리지 않는다")
  void laterIncrementKeepsExpire() {
    counter.increment(KEY, WINDOW);
    counter.increment(KEY, WINDOW);

    assertThat(counter.increment(KEY, 600_000L)).isEqualTo(3);
    assertThat(counter.remainingMillis(KEY)).isLessThanOrEqualTo(WINDOW);
  }

  @Test
  @DisplayName("만료 없이 남은 키에도 만료를 건다")
  void repairsKeyWithoutExpire() {
    redisTemplate.opsForValue().set(KEY, "4");

    assertThat(counter.increment(KEY, WINDOW)).isEqualTo(5);
    assertThat(counter.remainingMillis(KEY)).isPositive();
  }

  @Test
  @DisplayName("키가 없으면 남은 시간은 0")
  void noKeyMeansZero() {
    assertThat(counter.remainingMillis(KEY)).isZero();
  }
}
