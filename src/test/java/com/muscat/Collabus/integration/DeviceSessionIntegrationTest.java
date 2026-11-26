package com.muscat.Collabus.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.enums.role.SystemRole;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 두 기기에서 로그인한 뒤 재발급 · 로그아웃 · 비밀번호 변경이 세션별로 어떻게 갈리는지
 * 실제 Redis 와 HTTP 요청으로 확인한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Testcontainers
@EnabledIf(value = "dockerAvailable", disabledReason = "Docker 를 사용할 수 없는 환경에서는 건너뛴다")
@DisplayName("기기별 로그인 세션 통합 테스트")
class DeviceSessionIntegrationTest {

  private static final String EMAIL = "device@test.com";
  private static final String PASSWORD = "password1";

  @Container
  @ServiceConnection(name = "redis")
  static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private StringRedisTemplate redisTemplate;

  private Long userId;

  static boolean dockerAvailable() {
    try {
      return DockerClientFactory.instance().isDockerAvailable();
    } catch (Throwable e) {
      return false;
    }
  }

  @BeforeEach
  void setUp() {
    redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    userId = userRepository.save(User.builder()
        .email(EMAIL)
        .nickname("device")
        .password(passwordEncoder.encode(PASSWORD))
        .tag("0001")
        .displayName("device#0001")
        .role(SystemRole.USER)
        .build()).getId();
  }

  /** 로그인 한 번으로 받은 토큰 쌍 */
  private record Tokens(String access, String refresh) {

  }

  private Tokens login() throws Exception {
    String body = mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"%s","password":"%s"}
                """.formatted(EMAIL, PASSWORD)))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    JsonNode data = objectMapper.readTree(body).get("data");
    return new Tokens(data.get("accessToken").asText(), data.get("refreshToken").asText());
  }

  private ResultActions reissue(String refreshToken) throws Exception {
    return mockMvc.perform(post("/api/token/refresh")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"refreshToken":"%s"}
            """.formatted(refreshToken)));
  }

  private Tokens reissueOk(String refreshToken) throws Exception {
    String body = reissue(refreshToken).andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    JsonNode data = objectMapper.readTree(body).get("data");
    return new Tokens(data.get("accessToken").asText(), data.get("refreshToken").asText());
  }

  @Test
  @DisplayName("다른 기기에서 로그인해도 앞 기기의 재발급이 그대로 된다")
  void secondLoginDoesNotKickFirst() throws Exception {
    Tokens laptop = login();
    Tokens phone = login();

    reissueOk(laptop.refresh());
    reissueOk(phone.refresh());
  }

  @Test
  @DisplayName("재발급으로 밀려난 Refresh Token 은 다시 쓸 수 없다")
  void rotatedTokenIsRejected() throws Exception {
    Tokens laptop = login();
    Tokens rotated = reissueOk(laptop.refresh());

    reissue(laptop.refresh()).andExpect(status().isUnauthorized());
    reissueOk(rotated.refresh());
  }

  @Test
  @DisplayName("한 기기에서 로그아웃하면 그 기기만 끝나고 다른 기기는 이어서 쓴다")
  void logoutEndsOnlyThatDevice() throws Exception {
    Tokens laptop = login();
    Tokens phone = login();

    mockMvc.perform(post("/api/users/logout").header("Authorization", "Bearer " + laptop.access()))
        .andExpect(status().isOk());

    reissue(laptop.refresh()).andExpect(status().isUnauthorized());
    reissueOk(phone.refresh());
  }

  @Test
  @DisplayName("비밀번호를 바꾸면 모든 기기의 재발급이 막힌다")
  void passwordChangeEndsAllDevices() throws Exception {
    Tokens laptop = login();
    Tokens phone = login();

    mockMvc.perform(patch("/api/users/password/" + userId)
            .header("Authorization", "Bearer " + laptop.access())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"currentPassword":"%s","password":"newpassword2"}
                """.formatted(PASSWORD)))
        .andExpect(status().isOk());

    reissue(laptop.refresh()).andExpect(status().isUnauthorized());
    reissue(phone.refresh()).andExpect(status().isUnauthorized());
  }
}
