package com.muscat.Collabus.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.config.token.RefreshTokenService;
import com.muscat.Collabus.enums.role.SystemRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로그인부터 보호 API 호출까지 실제 필터 체인을 통과시키는 통합 테스트
 * Redis 는 컨테이너 없이 돌리기 위해 RefreshTokenService만 대역으로 바꾼다.
 * 그 외 시큐리티 필터, JWT 발급·검증, 컨트롤러, 서비스, JPA 는 실제 구현을 그대로 탄다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("인증 플로우 통합 테스트")
class AuthenticationFlowIntegrationTest {

  private static final String EMAIL = "integration@test.com";
  private static final String PASSWORD = "password123";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @MockBean
  private RefreshTokenService refreshTokenService;

  private Long userId;

  @BeforeEach
  void setUp() {
    when(refreshTokenService.isAccountLocked(anyString())).thenReturn(false);
    when(refreshTokenService.isBlacklisted(anyString())).thenReturn(false);

    User user = userRepository.save(User.builder()
        .email(EMAIL)
        .nickname("integration")
        .password(passwordEncoder.encode(PASSWORD))
        .tag("0001")
        .displayName("integration#0001")
        .role(SystemRole.USER)
        .build());
    userId = user.getId();
  }

  private String login() throws Exception {
    MvcResult result = mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"%s","password":"%s"}
                """.formatted(EMAIL, PASSWORD)))
        .andExpect(status().isOk())
        .andReturn();

    return objectMapper.readTree(result.getResponse().getContentAsString())
        .path("data").path("accessToken").asText();
  }

  @Test
  @DisplayName("토큰 없이 보호 API 를 호출하면 401")
  void protectedApi_WithoutToken_Returns401() throws Exception {
    // 기본 구현은 403 을 주는데, 클라이언트가 401 에만 재발급을 시도하므로 401 이어야 한다
    mockMvc.perform(get("/api/workspaces/joined"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.errorType").value("UNAUTHORIZED"));
  }

  @Test
  @DisplayName("위조된 토큰으로 호출하면 401")
  void protectedApi_WithTamperedToken_Returns401() throws Exception {
    mockMvc.perform(get("/api/workspaces/joined")
            .header("Authorization", "Bearer not-a-real-token"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.errorType").value("UNAUTHORIZED"));
  }

  @Test
  @DisplayName("로그인 후 발급받은 토큰으로 보호 API 를 호출하면 200")
  void loginThenCallProtectedApi() throws Exception {
    String accessToken = login();
    assertThat(accessToken).isNotBlank();

    mockMvc.perform(get("/api/workspaces/joined")
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("로그인 응답의 Access Token 에 인증에 필요한 클레임이 담긴다")
  void accessTokenCarriesClaims() throws Exception {
    String accessToken = login();

    String payload = new String(java.util.Base64.getUrlDecoder()
        .decode(accessToken.split("\\.")[1]));

    assertThat(objectMapper.readTree(payload).path("userId").asLong()).isEqualTo(userId);
    assertThat(objectMapper.readTree(payload).path("displayName").asText())
        .isEqualTo("integration#0001");
  }

  @Test
  @DisplayName("블랙리스트에 오른 토큰은 401")
  void blacklistedToken_Returns401() throws Exception {
    String accessToken = login();
    when(refreshTokenService.isBlacklisted(accessToken)).thenReturn(true);

    mockMvc.perform(get("/api/workspaces/joined")
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("남의 계정 정보를 수정하려 하면 403")
  void updateOtherUsersNickname_Returns403() throws Exception {
    String accessToken = login();

    // @PreAuthorize("#id == principal.userId or hasRole('ADMIN')") 가 막아야 한다
    mockMvc.perform(patch("/api/users/nickname/{id}", userId + 999)
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"nickname\":\"hijacked\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.errorType").value("FORBIDDEN"));
  }

  @Test
  @DisplayName("본인 계정 정보는 수정할 수 있다")
  void updateOwnNickname_Succeeds() throws Exception {
    String accessToken = login();

    mockMvc.perform(patch("/api/users/nickname/{id}", userId)
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"nickname\":\"renamed\"}"))
        .andExpect(status().isOk());

    assertThat(userRepository.findById(userId))
        .get()
        .extracting(User::getNickname)
        .isEqualTo("renamed");
  }

  @Test
  @DisplayName("잘못된 비밀번호로는 로그인할 수 없다")
  void login_WithWrongPassword_Fails() throws Exception {
    when(refreshTokenService.incrementLoginFailure(anyString())).thenReturn(1);

    mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"%s","password":"wrong-password"}
                """.formatted(EMAIL)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  @DisplayName("검증에 걸리는 요청 본문은 400 과 함께 문제 필드를 알려준다")
  void invalidRequestBody_Returns400() throws Exception {
    String accessToken = login();

    mockMvc.perform(post("/api/workspaces")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"workspaceName\":\"\",\"description\":\"desc\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorType").value("VALIDATION"));
  }

  @Test
  @DisplayName("매핑되지 않은 경로는 404")
  void unmappedPath_Returns404() throws Exception {
    String accessToken = login();

    mockMvc.perform(get("/api/does-not-exist")
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("계정이 잠기면 로그인이 막힌다")
  void login_WhenAccountLocked_Returns429() throws Exception {
    when(refreshTokenService.isAccountLocked(EMAIL)).thenReturn(true);

    mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"%s","password":"%s"}
                """.formatted(EMAIL, PASSWORD)))
        .andExpect(status().isTooManyRequests());
  }


}
