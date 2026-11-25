package com.muscat.Collabus.Todo.controller;

import static com.muscat.Collabus.support.LoginUser.login;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.muscat.Collabus.Todo.model.TodoRequestDto;
import com.muscat.Collabus.Todo.model.TodoResponseDto;
import com.muscat.Collabus.Todo.service.TodoService;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.enums.response.CommonResponse;
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

@ControllerSliceTest(TodoController.class)
@DisplayName("Todo 컨트롤러")
class TodoControllerTest {

  private static final String VALID_TODO = """
      {"taskId":3,"title":"시안 작업","dueDate":"2030-12-31"}
      """;

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private TodoService todoService;

  @Test
  @DisplayName("로그인하지 않으면 401 이고 서비스까지 가지 않는다")
  void unauthenticated() throws Exception {
    mockMvc.perform(get("/api/todo/1"))
        .andExpect(status().isUnauthorized());

    verifyNoInteractions(todoService);
  }

  @Test
  @DisplayName("생성 요청을 로그인 사용자 ID 와 함께 서비스로 넘긴다")
  void create() throws Exception {
    when(todoService.createTodo(any(), eq(LoginUser.ID)))
        .thenReturn(TodoResponseDto.builder().id(10L).title("시안 작업").build());

    mockMvc.perform(post("/api/todo").with(login())
            .contentType(MediaType.APPLICATION_JSON).content(VALID_TODO))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(10));

    ArgumentCaptor<TodoRequestDto> captor = ArgumentCaptor.forClass(TodoRequestDto.class);
    verify(todoService).createTodo(captor.capture(), eq(LoginUser.ID));
    assertThat(captor.getValue().getTaskId()).isEqualTo(3L);
  }

  @Test
  @DisplayName("제목이 비어 있으면 400 과 필드 메시지를 주고 서비스까지 가지 않는다")
  void createWithoutTitle() throws Exception {
    mockMvc.perform(post("/api/todo").with(login())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"taskId":3,"title":" ","dueDate":"2030-12-31"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("title: 할 일 제목은 필수입니다."));

    verifyNoInteractions(todoService);
  }

  @Test
  @DisplayName("날짜 형식이 틀린 본문은 400 이다")
  void createWithBrokenDate() throws Exception {
    mockMvc.perform(post("/api/todo").with(login())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"taskId":3,"title":"시안","dueDate":"내일"}
                """))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(todoService);
  }

  @Test
  @DisplayName("목록은 taskId 가 없으면 400, 있으면 기본 페이지 20개 · 마감일 정렬로 넘긴다")
  void listByTask() throws Exception {
    mockMvc.perform(get("/api/todo").with(login()))
        .andExpect(status().isBadRequest());

    mockMvc.perform(get("/api/todo").with(login()).param("taskId", "3"))
        .andExpect(status().isOk());

    ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
    verify(todoService).getTodosByTask(eq(3L), eq(LoginUser.ID), isNull(), pageable.capture());
    assertThat(pageable.getValue().getPageSize()).isEqualTo(20);
    assertThat(pageable.getValue().getSort().getOrderFor("dueDate")).isNotNull();
  }

  @Test
  @DisplayName("경로의 ID 가 숫자가 아니면 400 이다")
  void nonNumericId() throws Exception {
    mockMvc.perform(get("/api/todo/abc").with(login()))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(todoService);
  }

  @Test
  @DisplayName("서비스가 수정 충돌을 내면 409 와 안내 문구를 준다")
  void updateConflict() throws Exception {
    when(todoService.updateTodo(eq(1L), any(), eq(LoginUser.ID)))
        .thenThrow(new BusinessException(CommonResponse.EDIT_CONFLICT));

    mockMvc.perform(patch("/api/todo/1").with(login())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"고친 제목","dueDate":"2030-12-31","version":0}
                """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value(CommonResponse.EDIT_CONFLICT.getMessage()));
  }

  @Test
  @DisplayName("담당자 변경은 두 경로 값과 로그인 사용자를 넘기고 본문 없이 200 이다")
  void changeAssignee() throws Exception {
    mockMvc.perform(patch("/api/todo/1/assignee/5").with(login()))
        .andExpect(status().isOk())
        .andExpect(content().string(""));

    verify(todoService).changeAssignee(1L, 5L, LoginUser.ID);
  }
}
