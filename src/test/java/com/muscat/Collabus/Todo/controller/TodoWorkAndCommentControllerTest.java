package com.muscat.Collabus.Todo.controller;

import static com.muscat.Collabus.support.LoginUser.login;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.muscat.Collabus.Todo.service.TodoCommentService;
import com.muscat.Collabus.Todo.service.TodoWorkService;
import com.muscat.Collabus.support.ControllerSliceTest;
import com.muscat.Collabus.support.LoginUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ControllerSliceTest({TodoWorkController.class, TodoCommentController.class})
@DisplayName("작업 내용 · 댓글 컨트롤러")
class TodoWorkAndCommentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private TodoWorkService todoWorkService;

  @MockitoBean
  private TodoCommentService todoCommentService;

  @Test
  @DisplayName("작업 내용은 todoId 가 없으면 400 이다")
  void workWithoutTodoId() throws Exception {
    mockMvc.perform(post("/api/todo/works").with(login())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"작업","content":"내용"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("필수 요청 파라미터가 없습니다: todoId"));

    verifyNoInteractions(todoWorkService);
  }

  @Test
  @DisplayName("작업 내용은 제목이 비면 400, 있으면 todoId 와 로그인 사용자로 넘긴다")
  void createWork() throws Exception {
    mockMvc.perform(post("/api/todo/works").with(login()).param("todoId", "3")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"","content":"내용"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("title: 작업 제목은 필수입니다."));

    mockMvc.perform(post("/api/todo/works").with(login()).param("todoId", "3")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"작업","content":"내용"}
                """))
        .andExpect(status().isOk());

    verify(todoWorkService).createWork(eq(3L), any(), eq(LoginUser.ID));
  }

  @Test
  @DisplayName("댓글은 2000자를 넘으면 400 이고 서비스까지 가지 않는다")
  void commentTooLong() throws Exception {
    String body = "{\"content\":\"" + "가".repeat(2001) + "\"}";

    mockMvc.perform(post("/api/todo/comments").with(login()).param("todoId", "3")
            .contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("content: 댓글은 2000자 이하여야 합니다."));

    verifyNoInteractions(todoCommentService);
  }
}
