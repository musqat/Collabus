package com.muscat.Collabus.Workspace.controller;

import static com.muscat.Collabus.support.LoginUser.login;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.muscat.Collabus.Workspace.service.WorkspaceService;
import com.muscat.Collabus.support.ControllerSliceTest;
import com.muscat.Collabus.support.LoginUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ControllerSliceTest(WorkspaceController.class)
@DisplayName("워크스페이스 컨트롤러")
class WorkspaceControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private WorkspaceService workspaceService;

  @Test
  @DisplayName("이름이 비어 있으면 400 과 필드 메시지를 준다")
  void createWithoutName() throws Exception {
    mockMvc.perform(post("/api/workspaces").with(login())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"workspaceName":"","description":"설명"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("workspaceName: 워크스페이스 이름은 필수입니다."));

    verifyNoInteractions(workspaceService);
  }

  @Test
  @DisplayName("참여 목록은 페이지 · 정렬 파라미터를 그대로 넘긴다")
  void joinedWithPaging() throws Exception {
    mockMvc.perform(get("/api/workspaces/joined").with(login())
            .param("page", "2").param("size", "5").param("sort", "workspaceName,asc"))
        .andExpect(status().isOk());

    ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
    verify(workspaceService).getJoinedWorkspaces(eq(LoginUser.ID), pageable.capture());
    assertThat(pageable.getValue().getPageNumber()).isEqualTo(2);
    assertThat(pageable.getValue().getPageSize()).isEqualTo(5);
    assertThat(pageable.getValue().getSort().getOrderFor("workspaceName")).isNotNull();
  }

  @Test
  @DisplayName("삭제는 경로의 ID 와 로그인 사용자를 넘긴다")
  void deleteWorkspace() throws Exception {
    mockMvc.perform(delete("/api/workspaces/4").with(login()))
        .andExpect(status().isOk());

    verify(workspaceService).deleteWorkspace(4L, LoginUser.ID);
  }
}
