package com.muscat.Collabus.User.controller;

import static com.muscat.Collabus.support.LoginUser.admin;
import static com.muscat.Collabus.support.LoginUser.login;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.muscat.Collabus.User.service.UserService;
import com.muscat.Collabus.support.ControllerSliceTest;
import com.muscat.Collabus.support.LoginUser;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ControllerSliceTest(UserController.class)
@DisplayName("User 컨트롤러")
class UserControllerTest {

  private static final String NEW_USER = """
      {"email":"new@test.com","nickname":"new","password":"password1"}
      """;
  private static final String NICKNAME = """
      {"nickname":"바뀐이름"}
      """;

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserService userService;

  @Test
  @DisplayName("회원가입은 로그인 없이 되고 201 이다")
  void register() throws Exception {
    mockMvc.perform(post("/api/users/register")
            .contentType(MediaType.APPLICATION_JSON).content(NEW_USER))
        .andExpect(status().isCreated());

    verify(userService).registerUser(any());
  }

  @Test
  @DisplayName("비밀번호에 숫자가 없으면 400 과 규칙 문구를 준다")
  void registerWeakPassword() throws Exception {
    mockMvc.perform(post("/api/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"new@test.com","nickname":"new","password":"onlyletters"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("password: 비밀번호는 영문자와 숫자를 모두 포함해야 합니다."));

    verifyNoInteractions(userService);
  }

  @Test
  @DisplayName("없는 사용자를 조회하면 404 이다")
  void userNotFound() throws Exception {
    when(userService.findByDisplayName("nobody#0000")).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/users/nobody#0000").with(login()))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("자기 닉네임은 바꿀 수 있고 남의 닉네임은 403 이다")
  void updateNicknameOwnership() throws Exception {
    mockMvc.perform(patch("/api/users/nickname/" + LoginUser.ID).with(login())
            .contentType(MediaType.APPLICATION_JSON).content(NICKNAME))
        .andExpect(status().isOk());

    mockMvc.perform(patch("/api/users/nickname/99").with(login())
            .contentType(MediaType.APPLICATION_JSON).content(NICKNAME))
        .andExpect(status().isForbidden());

    verify(userService).updateNickname(LoginUser.ID, "바뀐이름");
  }

  @Test
  @DisplayName("남의 이메일로 탈퇴 요청을 보내면 403 이다")
  void deleteOtherUser() throws Exception {
    mockMvc.perform(delete("/api/users/delete").with(login()).param("email", "other@test.com"))
        .andExpect(status().isForbidden());

    verifyNoInteractions(userService);
  }

  @Test
  @DisplayName("관리자는 남의 닉네임도 바꿀 수 있다")
  void adminUpdatesOtherNickname() throws Exception {
    mockMvc.perform(patch("/api/users/nickname/99").with(admin())
            .contentType(MediaType.APPLICATION_JSON).content(NICKNAME))
        .andExpect(status().isOk());

    verify(userService).updateNickname(99L, "바뀐이름");
  }

  @Test
  @DisplayName("관리자 계정 생성은 관리자만 할 수 있다")
  void createAdminOnlyByAdmin() throws Exception {
    mockMvc.perform(post("/api/users/create-admin").with(login())
            .contentType(MediaType.APPLICATION_JSON).content(NEW_USER))
        .andExpect(status().isForbidden());

    mockMvc.perform(post("/api/users/create-admin").with(admin())
            .contentType(MediaType.APPLICATION_JSON).content(NEW_USER))
        .andExpect(status().isCreated());

    verify(userService).createAdmin(any());
  }
}
