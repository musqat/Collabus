package com.muscat.Collabus.config.token;

import static org.assertj.core.api.Assertions.assertThat;

import com.muscat.Collabus.config.ratelimit.RedisWindowCounter;
import java.util.concurrent.TimeUnit;
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
 * 기기별 Refresh Token 저장을 실제 Redis 에서 확인한다.
 */
@Testcontainers
@EnabledIf(value = "dockerAvailable", disabledReason = "Docker 를 사용할 수 없는 환경에서는 건너뛴다")
@DisplayName("기기별 Refresh Token 저장")
class RefreshTokenSessionStoreTest {

  private static final String EMAIL = "user@test.com";
  private static final long TTL = 60_000L;

  @Container
  static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

  private static LettuceConnectionFactory connectionFactory;
  private static StringRedisTemplate redisTemplate;
  private static RefreshTokenServiceImpl store;

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
    store = new RefreshTokenServiceImpl(redisTemplate, new RedisWindowCounter(redisTemplate));
  }

  @AfterAll
  static void disconnect() {
    connectionFactory.destroy();
  }

  @BeforeEach
  void flush() {
    redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
  }

  @Test
  @DisplayName("기기마다 따로 저장되고 다른 기기의 로그인이 덮어쓰지 않는다")
  void sessionsAreIndependent() {
    store.saveRefreshToken(EMAIL, "laptop", "rt-laptop", TTL);
    store.saveRefreshToken(EMAIL, "phone", "rt-phone", TTL);

    assertThat(store.getRefreshToken(EMAIL, "laptop")).contains("rt-laptop");
    assertThat(store.getRefreshToken(EMAIL, "phone")).contains("rt-phone");
  }

  @Test
  @DisplayName("같은 세션에 다시 저장하면 그 세션의 토큰만 바뀐다")
  void rotationStaysInSession() {
    store.saveRefreshToken(EMAIL, "laptop", "rt-1", TTL);
    store.saveRefreshToken(EMAIL, "phone", "rt-phone", TTL);

    store.saveRefreshToken(EMAIL, "laptop", "rt-2", TTL);

    assertThat(store.getRefreshToken(EMAIL, "laptop")).contains("rt-2");
    assertThat(store.getRefreshToken(EMAIL, "phone")).contains("rt-phone");
  }

  @Test
  @DisplayName("한 세션을 지워도 다른 세션은 남는다")
  void deleteOneSession() {
    store.saveRefreshToken(EMAIL, "laptop", "rt-laptop", TTL);
    store.saveRefreshToken(EMAIL, "phone", "rt-phone", TTL);

    store.deleteRefreshToken(EMAIL, "laptop");

    assertThat(store.getRefreshToken(EMAIL, "laptop")).isEmpty();
    assertThat(store.getRefreshToken(EMAIL, "phone")).contains("rt-phone");
  }

  @Test
  @DisplayName("전체 삭제는 모든 세션과 세션 목록을 지운다")
  void deleteAllSessions() {
    store.saveRefreshToken(EMAIL, "laptop", "rt-laptop", TTL);
    store.saveRefreshToken(EMAIL, "phone", "rt-phone", TTL);
    store.saveRefreshToken("other@test.com", "laptop", "rt-other", TTL);

    store.deleteAllRefreshTokens(EMAIL);

    assertThat(store.getRefreshToken(EMAIL, "laptop")).isEmpty();
    assertThat(store.getRefreshToken(EMAIL, "phone")).isEmpty();
    assertThat(redisTemplate.hasKey("RTS:" + EMAIL)).isFalse();
    assertThat(store.getRefreshToken("other@test.com", "laptop")).contains("rt-other");
  }

  @Test
  @DisplayName("세션이 상한을 넘으면 가장 오래 쓰지 않은 세션부터 지운다")
  void evictsOldestOverLimit() throws InterruptedException {
    for (int i = 0; i <= RefreshTokenServiceImpl.MAX_SESSIONS; i++) {
      store.saveRefreshToken(EMAIL, "s" + i, "rt-" + i, TTL);
      Thread.sleep(2);
    }

    assertThat(store.getRefreshToken(EMAIL, "s0")).isEmpty();
    assertThat(store.getRefreshToken(EMAIL, "s1")).contains("rt-1");
    assertThat(store.getRefreshToken(EMAIL, "s" + RefreshTokenServiceImpl.MAX_SESSIONS))
        .contains("rt-" + RefreshTokenServiceImpl.MAX_SESSIONS);
    assertThat(redisTemplate.opsForZSet().zCard("RTS:" + EMAIL))
        .isEqualTo(RefreshTokenServiceImpl.MAX_SESSIONS);
  }

  @Test
  @DisplayName("재발급으로 다시 쓴 세션은 오래된 세션으로 치지 않는다")
  void recentlyUsedSessionSurvives() throws InterruptedException {
    for (int i = 0; i < RefreshTokenServiceImpl.MAX_SESSIONS; i++) {
      store.saveRefreshToken(EMAIL, "s" + i, "rt-" + i, TTL);
      Thread.sleep(2);
    }
    store.saveRefreshToken(EMAIL, "s0", "rt-0-rotated", TTL);
    Thread.sleep(2);

    store.saveRefreshToken(EMAIL, "new", "rt-new", TTL);

    assertThat(store.getRefreshToken(EMAIL, "s0")).contains("rt-0-rotated");
    assertThat(store.getRefreshToken(EMAIL, "s1")).isEmpty();
  }

  @Test
  @DisplayName("토큰과 세션 목록에 만료가 걸린다")
  void keysExpire() {
    store.saveRefreshToken(EMAIL, "laptop", "rt-laptop", TTL);

    assertThat(redisTemplate.getExpire("RT:" + EMAIL + ":laptop", TimeUnit.MILLISECONDS))
        .isBetween(TTL - 1_000, TTL);
    assertThat(redisTemplate.getExpire("RTS:" + EMAIL, TimeUnit.MILLISECONDS))
        .isBetween(TTL - 1_000, TTL);
  }
}
