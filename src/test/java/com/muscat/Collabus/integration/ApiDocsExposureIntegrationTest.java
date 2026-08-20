package com.muscat.Collabus.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.muscat.Collabus.config.token.RefreshTokenService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * API 문서가 스위치대로 열리고 닫히는지 확인한다.
 * 기본값은 열어 두고 스펙 문서로 쓴다. 실제 호출은 문서와 무관하게 인증을 요구한다.
 */
@DisplayName("API 문서 노출")
class ApiDocsExposureIntegrationTest {

  @Nested
  @SpringBootTest
  @AutoConfigureMockMvc
  @TestPropertySource(properties = "springdoc.api-docs.enabled=false")
  @DisplayName("꺼져 있을 때")
  class Disabled {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("api-docs 는 인증 없이 볼 수 없다")
    void apiDocs_Blocked() throws Exception {
      mockMvc.perform(get("/v3/api-docs"))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("swagger-ui 도 인증 없이 볼 수 없다")
    void swaggerUi_Blocked() throws Exception {
      mockMvc.perform(get("/swagger-ui/index.html"))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("헬스체크는 그대로 인증 없이 닿는다")
    void health_StillOpen() throws Exception {
      // Redis 가 없는 테스트 환경에서는 503 이 난다. 여기서는 401 이 아닌지만 본다
      mockMvc.perform(get("/actuator/health"))
          .andExpect(result ->
              assertThat(result.getResponse().getStatus()).isNotEqualTo(401));
    }
  }

  @Nested
  @SpringBootTest
  @AutoConfigureMockMvc
  @TestPropertySource(properties = "springdoc.api-docs.enabled=true")
  @DisplayName("켜져 있을 때")
  class Enabled {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("api-docs 를 인증 없이 볼 수 있다")
    void apiDocs_Open() throws Exception {
      mockMvc.perform(get("/v3/api-docs"))
          .andExpect(status().isOk());
    }
  }
}
