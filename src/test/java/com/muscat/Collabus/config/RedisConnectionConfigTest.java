package com.muscat.Collabus.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@DisplayName("Redis 연결 설정")
class RedisConnectionConfigTest {

  private final ApplicationContextRunner runner = new ApplicationContextRunner()
      .withConfiguration(AutoConfigurations.of(RedisAutoConfiguration.class))
      .withUserConfiguration(RedisConnectionConfig.class)
      .withPropertyValues("spring.data.redis.timeout=3000");

  @Test
  @DisplayName("앱의 Redis 연결은 꺼낼 때마다 검증하고 명령은 3초에 끊는다")
  void validatesSharedConnection() {
    runner.run(context -> {
      LettuceConnectionFactory factory = context.getBean(LettuceConnectionFactory.class);

      assertThat(factory.getValidateConnection()).isTrue();
      assertThat(factory.getClientConfiguration().getCommandTimeout())
          .isEqualTo(Duration.ofSeconds(3));
    });
  }
}
