package com.muscat.Collabus.config.token;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.muscat.Collabus.support.ControllerSliceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ControllerSliceTest(TokenController.class)
@DisplayName("토큰 재발급 컨트롤러")
class TokenControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private TokenReissueService tokenReissueService;

  @Test
  @DisplayName("로그인 없이 부를 수 있고 Refresh Token 을 그대로 넘긴다")
  void reissueWithoutLogin() throws Exception {
    mockMvc.perform(post("/api/token/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"refreshToken":"rt-value"}
                """))
        .andExpect(status().isOk());

    verify(tokenReissueService).reissue("rt-value");
  }

  @Test
  @DisplayName("Refresh Token 이 비어 있으면 400 이다")
  void blankToken() throws Exception {
    mockMvc.perform(post("/api/token/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"refreshToken":""}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("refreshToken: Refresh Token 은 필수입니다."));

    verifyNoInteractions(tokenReissueService);
  }
}
