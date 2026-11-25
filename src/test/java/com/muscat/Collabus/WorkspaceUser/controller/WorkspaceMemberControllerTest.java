package com.muscat.Collabus.WorkspaceUser.controller;

import static com.muscat.Collabus.support.LoginUser.login;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.muscat.Collabus.WorkspaceUser.service.WorkspaceUserInviteService;
import com.muscat.Collabus.WorkspaceUser.service.WorkspaceUserService;
import com.muscat.Collabus.enums.role.WorkspaceRole;
import com.muscat.Collabus.support.ControllerSliceTest;
import com.muscat.Collabus.support.LoginUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ControllerSliceTest({WorkspaceUserController.class, WorkspaceUserInviteController.class})
@DisplayName("워크스페이스 멤버 · 초대 컨트롤러")
class WorkspaceMemberControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private WorkspaceUserService workspaceUserService;

  @MockitoBean
  private WorkspaceUserInviteService inviteService;

  @Test
  @DisplayName("역할 변경은 정해진 역할만 받고 그 밖의 값은 400 이다")
  void updateRole() throws Exception {
    mockMvc.perform(put("/api/workspaces/1/users/9/role").with(login()).param("newRole", "BOSS"))
        .andExpect(status().isBadRequest());

    mockMvc.perform(put("/api/workspaces/1/users/9/role").with(login()).param("newRole", "MANAGER"))
        .andExpect(status().isOk());

    verify(workspaceUserService).updateUserRole(1L, 9L, WorkspaceRole.MANAGER, LoginUser.ID);
  }

  @Test
  @DisplayName("초대는 역할이 빠지면 400 이고 서비스까지 가지 않는다")
  void inviteWithoutRole() throws Exception {
    mockMvc.perform(post("/api/workspaces/1/invites").with(login())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"userId":9}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("role: 역할은 필수입니다."));

    verifyNoInteractions(inviteService);
  }

  @Test
  @DisplayName("초대는 초대한 사람 · 워크스페이스 순서로 넘긴다")
  void invite() throws Exception {
    mockMvc.perform(post("/api/workspaces/1/invites").with(login())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"userId":9,"role":"MEMBER"}
                """))
        .andExpect(status().isOk());

    verify(inviteService).inviteUserToWorkspace(eq(LoginUser.ID), eq(1L), any());
  }

  @Test
  @DisplayName("초대 수락은 초대 ID 와 로그인 사용자를 넘긴다")
  void acceptInvite() throws Exception {
    mockMvc.perform(post("/api/workspaces/invites/5/accept").with(login()))
        .andExpect(status().isOk());

    verify(inviteService).acceptInvite(5L, LoginUser.ID);
  }
}
