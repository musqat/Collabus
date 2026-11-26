package com.muscat.Collabus.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.muscat.Collabus.support.BlackholeProxy;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 기존 연결만 소리 없이 끊겼을 때 연결 검증이 새 연결로 바꿔 주는지 실제 Redis 로 확인한다.
 */
@Testcontainers
@EnabledIf(value = "dockerAvailable", disabledReason = "Docker 를 사용할 수 없는 환경에서는 건너뛴다")
@DisplayName("끊긴 Redis 연결 회복")
class RedisStaleConnectionTest {

  private static final Duration COMMAND_TIMEOUT = Duration.ofSeconds(1);

  @Container
  static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

  private BlackholeProxy proxy;
  private LettuceConnectionFactory factory;

  static boolean dockerAvailable() {
    try {
      return DockerClientFactory.instance().isDockerAvailable();
    } catch (Throwable e) {
      return false;
    }
  }

  @AfterEach
  void tearDown() throws Exception {
    if (factory != null) {
      factory.destroy();
    }
    if (proxy != null) {
      proxy.close();
    }
  }

  private StringRedisTemplate templateThroughProxy(boolean validateConnection) throws Exception {
    proxy = new BlackholeProxy(redis.getHost(), redis.getMappedPort(6379));
    factory = new LettuceConnectionFactory(
        new RedisStandaloneConfiguration("127.0.0.1", proxy.port()),
        LettuceClientConfiguration.builder().commandTimeout(COMMAND_TIMEOUT).build());
    factory.setValidateConnection(validateConnection);
    factory.afterPropertiesSet();
    factory.start();
    StringRedisTemplate template = new StringRedisTemplate(factory);
    template.opsForValue().set("k", "v");
    return template;
  }

  @Test
  @DisplayName("연결 검증을 켜면 기존 연결이 끊겨도 새 연결로 바꿔 명령이 성공한다")
  void recoversWithValidation() throws Exception {
    StringRedisTemplate template = templateThroughProxy(true);

    proxy.blackholeExisting();

    assertThat(template.opsForValue().get("k")).isEqualTo("v");
  }

  @Test
  @DisplayName("연결 검증이 없으면 끊긴 연결을 계속 써서 시간 초과가 난다")
  void timesOutWithoutValidation() throws Exception {
    StringRedisTemplate template = templateThroughProxy(false);

    proxy.blackholeExisting();

    assertThatThrownBy(() -> template.opsForValue().get("k"))
        .isInstanceOf(QueryTimeoutException.class);
  }
}
