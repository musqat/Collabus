package com.muscat.Collabus.config;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * Redis 공유 연결을 쓸 때마다 살아 있는지 확인한다.
 */
@Configuration
public class RedisConnectionConfig {

  // 공유 연결을 꺼낼 때 PING 으로 확인하고, 응답이 없으면 새 연결로 교체
  @Bean
  static BeanPostProcessor redisConnectionValidation() {
    return new BeanPostProcessor() {
      @Override
      public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (bean instanceof LettuceConnectionFactory factory) {
          factory.setValidateConnection(true);
        }
        return bean;
      }
    };
  }
}
