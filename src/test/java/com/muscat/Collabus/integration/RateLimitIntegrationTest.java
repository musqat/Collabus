package com.muscat.Collabus.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * IP 단위 제한을 시큐리티 필터부터 실제 Redis 까지 통과시켜 확인한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Testcontainers
@EnabledIf(value = "dockerAvailable", disabledReason = "Docker 를 사용할 수 없는 환경에서는 건너뛴다")
@TestPropertySource(properties = {
    "rate-limit.enabled=true",
    "rate-limit.client-ip-header=X-Real-IP",
    "rate-limit.login-per-minute=3",
    "rate-limit.register-per-hour=2"
})
@DisplayName("IP 단위 요청 제한 통합 테스트")
class RateLimitIntegrationTest {

  private static final String ORIGIN = "http://localhost:3000";

  @Container
  @ServiceConnection(name = "redis")
  static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private StringRedisTemplate redisTemplate;

  static boolean dockerAvailable() {
    try {
      return DockerClientFactory.instance().isDockerAvailable();
    } catch (Throwable e) {
      return false;
    }
  }

  @BeforeEach
  void flush() {
    redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
  }

  private ResultActions login(String ip, String forwardedFor) throws Exception {
    var request = post("/api/users/login")
        .header("X-Real-IP", ip)
        .header("Origin", ORIGIN)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"email":"nobody@test.com","password":"password1"}
            """);
    if (forwardedFor != null) {
      request.header("X-Forwarded-For", forwardedFor);
    }
    return mockMvc.perform(request);
  }

  private ResultActions register(String ip, int n) throws Exception {
    return mockMvc.perform(post("/api/users/register")
        .header("X-Real-IP", ip)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"email":"new%d@test.com","nickname":"new%d","password":"password1"}
            """.formatted(n, n)));
  }

  @Test
  @DisplayName("한도까지는 원래 응답, 넘으면 429 와 남은 초를 CORS 헤더와 함께 준다")
  void loginLimit() throws Exception {
    for (int i = 0; i < 3; i++) {
      login("198.51.100.1", null).andExpect(status().isNotFound());
    }

    login("198.51.100.1", null)
        .andExpect(status().isTooManyRequests())
        .andExpect(header().exists("Retry-After"))
        .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN))
        .andExpect(jsonPath("$.statusCode").value("429"));
  }

  @Test
  @DisplayName("다른 IP 는 영향을 받지 않는다")
  void otherIpUnaffected() throws Exception {
    for (int i = 0; i < 4; i++) {
      login("198.51.100.1", null);
    }

    login("198.51.100.2", null).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("X-Forwarded-For 를 바꿔 보내도 같은 IP 로 센다")
  void forgedForwardedForDoesNotBypass() throws Exception {
    for (int i = 0; i < 3; i++) {
      login("198.51.100.1", "10.0.0." + i);
    }

    login("198.51.100.1", "10.0.0.99").andExpect(status().isTooManyRequests());
  }

  @Test
  @DisplayName("회원가입은 1시간 한도를 따로 센다")
  void registerLimit() throws Exception {
    register("198.51.100.1", 1).andExpect(status().isCreated());
    register("198.51.100.1", 2).andExpect(status().isCreated());

    register("198.51.100.1", 3).andExpect(status().isTooManyRequests());
  }
}
