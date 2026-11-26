package com.muscat.Collabus.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Task.entity.TaskUser;
import com.muscat.Collabus.Task.entity.TaskUserPk;
import com.muscat.Collabus.Task.repository.TaskRepository;
import com.muscat.Collabus.Task.repository.TaskUserRepository;
import com.muscat.Collabus.Todo.entity.Todo;
import com.muscat.Collabus.Todo.repository.TodoRepository;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.Workspace.entity.Workspace;
import com.muscat.Collabus.Workspace.repository.WorkspaceRepository;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUser;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUserPk;
import com.muscat.Collabus.WorkspaceUser.repository.WorkspaceUserRepository;
import com.muscat.Collabus.config.jwt.JwtUtil;
import com.muscat.Collabus.config.token.RefreshTokenService;
import com.muscat.Collabus.enums.role.SystemRole;
import com.muscat.Collabus.enums.role.TaskRole;
import com.muscat.Collabus.enums.role.WorkspaceRole;
import com.muscat.Collabus.enums.status.TodoStatus;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/**
 * 두 사람이 같은 버전을 보고 차례로 저장하는 흐름을 HTTP 요청으로 확인한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("수정 충돌 통합 테스트")
class OptimisticLockIntegrationTest {

  private static final LocalDate DUE = LocalDate.now().plusDays(7);

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private WorkspaceRepository workspaceRepository;

  @Autowired
  private WorkspaceUserRepository workspaceUserRepository;

  @Autowired
  private TaskRepository taskRepository;

  @Autowired
  private TaskUserRepository taskUserRepository;

  @Autowired
  private TodoRepository todoRepository;

  @MockitoBean
  private RefreshTokenService refreshTokenService;

  private String bearer;
  private Long taskId;
  private Long todoId;

  @BeforeEach
  void setUp() {
    when(refreshTokenService.isBlacklisted(anyString())).thenReturn(false);

    User master = userRepository.save(User.builder()
        .email("master@test.com")
        .nickname("master")
        .password("로그인을 하지 않으므로 쓰이지 않는다")
        .tag("0001")
        .displayName("master#0001")
        .role(SystemRole.USER)
        .build());
    bearer = "Bearer " + jwtUtil.generateToken(
        master.getId(), master.getEmail(), master.getRole().name(), master.getDisplayName(), "test-session");

    Workspace workspace = workspaceRepository.save(Workspace.builder()
        .workspaceName("충돌 확인용")
        .description("수정 충돌")
        .founder(master)
        .build());
    workspaceUserRepository.save(WorkspaceUser.builder()
        .id(new WorkspaceUserPk(workspace.getId(), master.getId()))
        .workspace(workspace)
        .user(master)
        .role(WorkspaceRole.MASTER)
        .build());

    Task task = taskRepository.save(Task.builder()
        .workspace(workspace)
        .title("원래 Task")
        .dueDate(DUE)
        .taskManager(master)
        .build());
    taskUserRepository.save(TaskUser.builder()
        .id(new TaskUserPk(task.getId(), master.getId()))
        .task(task)
        .user(master)
        .role(TaskRole.MANAGER)
        .build());
    taskId = task.getId();

    todoId = todoRepository.save(Todo.builder()
        .task(task)
        .title("원래 Todo")
        .status(TodoStatus.IN_PROGRESS)
        .dueDate(DUE)
        .assignee(master)
        .build()).getId();
    todoRepository.flush();
  }

  private ResultActions patchJson(String url, String title, long version) throws Exception {
    return mockMvc.perform(patch(url)
        .header("Authorization", bearer)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"title":"%s","dueDate":"%s","version":%d}
            """.formatted(title, DUE, version)));
  }

  @Test
  @DisplayName("Todo 를 같은 버전으로 두 번 저장하면 두 번째는 409 이고 첫 수정이 남는다")
  void todoSecondSaveWithSameVersionConflicts() throws Exception {
    mockMvc.perform(get("/api/todo/" + todoId).header("Authorization", bearer))
        .andExpect(jsonPath("$.data.version").value(0));

    patchJson("/api/todo/" + todoId, "A 가 고친 제목", 0)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.version").value(1));

    patchJson("/api/todo/" + todoId, "B 가 고친 제목", 0)
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value(
            "다른 사람이 먼저 수정했습니다. 새로 불러온 내용을 확인한 뒤 다시 수정해주세요."));

    assertThat(todoRepository.findById(todoId).orElseThrow().getTitle()).isEqualTo("A 가 고친 제목");
  }

  @Test
  @DisplayName("새 버전을 받아 다시 저장하면 성공한다")
  void todoSaveWithFreshVersionSucceeds() throws Exception {
    patchJson("/api/todo/" + todoId, "A 가 고친 제목", 0).andExpect(status().isOk());

    patchJson("/api/todo/" + todoId, "B 가 다시 고친 제목", 1)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.version").value(2));
  }

  @Test
  @DisplayName("Task 도 같은 버전으로 두 번 저장하면 두 번째는 409 다")
  void taskSecondSaveWithSameVersionConflicts() throws Exception {
    patchJson("/api/tasks/" + taskId, "A 가 고친 Task", 0)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.version").value(1));

    patchJson("/api/tasks/" + taskId, "B 가 고친 Task", 0)
        .andExpect(status().isConflict());

    assertThat(taskRepository.findById(taskId).orElseThrow().getTitle()).isEqualTo("A 가 고친 Task");
  }

  @Test
  @DisplayName("버전 없이 보낸 수정은 400 이다")
  void missingVersionIsBadRequest() throws Exception {
    mockMvc.perform(patch("/api/todo/" + todoId)
            .header("Authorization", bearer)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"버전 없음","dueDate":"%s"}
                """.formatted(DUE)))
        .andExpect(status().isBadRequest());
  }
}
