package com.muscat.Collabus.Task.controller;

import static com.muscat.Collabus.support.LoginUser.login;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.muscat.Collabus.Task.service.TaskService;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.enums.response.CommonResponse;
import com.muscat.Collabus.support.ControllerSliceTest;
import com.muscat.Collabus.support.LoginUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ControllerSliceTest(TaskController.class)
@DisplayName("Task 컨트롤러")
class TaskControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private TaskService taskService;

  @Test
  @DisplayName("마감일이 오늘 이전이면 400 과 필드 메시지를 주고 서비스까지 가지 않는다")
  void createWithPastDueDate() throws Exception {
    mockMvc.perform(post("/api/tasks").with(login())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"기획","dueDate":"2020-01-01","workspaceId":1}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("dueDate: 마감일은 오늘 이후여야 합니다."));

    verifyNoInteractions(taskService);
  }

  @Test
  @DisplayName("워크스페이스 ID 가 없으면 400 이다")
  void createWithoutWorkspace() throws Exception {
    mockMvc.perform(post("/api/tasks").with(login())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"기획","dueDate":"2030-12-31"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("workspaceId: 워크스페이스 ID는 필수입니다."));

    verifyNoInteractions(taskService);
  }

  @Test
  @DisplayName("목록 검색어를 서비스로 넘긴다")
  void listWithKeyword() throws Exception {
    mockMvc.perform(get("/api/tasks/workspaces/1/tasks").with(login()).param("keyword", "기획"))
        .andExpect(status().isOk());

    verify(taskService).getTasksByWorkspace(eq(1L), eq(LoginUser.ID), eq("기획"), any());
  }

  @Test
  @DisplayName("멤버 추가는 대상 사용자가 없으면 400, 있으면 그대로 넘긴다")
  void assignMember() throws Exception {
    mockMvc.perform(post("/api/tasks/1/members").with(login()))
        .andExpect(status().isBadRequest());

    mockMvc.perform(post("/api/tasks/1/members").with(login()).param("targetUserId", "9"))
        .andExpect(status().isOk());

    verify(taskService).assignUserToTask(1L, 9L, LoginUser.ID);
  }

  @Test
  @DisplayName("매니저 지정은 새 매니저 ID 를 넘긴다")
  void assignManager() throws Exception {
    mockMvc.perform(patch("/api/tasks/1/manager").with(login()).param("newManagerId", "4"))
        .andExpect(status().isOk());

    verify(taskService).assignTaskManager(1L, 4L, LoginUser.ID);
  }

  @Test
  @DisplayName("볼 권한이 없다는 서비스 예외는 403 과 그 문구로 나간다")
  void viewDenied() throws Exception {
    when(taskService.getTask(eq(1L), eq(LoginUser.ID)))
        .thenThrow(new BusinessException(CommonResponse.TASK_VIEW_DENIED));

    mockMvc.perform(get("/api/tasks/1").with(login()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value(CommonResponse.TASK_VIEW_DENIED.getMessage()));
  }
}
