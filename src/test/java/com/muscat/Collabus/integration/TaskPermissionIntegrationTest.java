package com.muscat.Collabus.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Task.entity.TaskUser;
import com.muscat.Collabus.Task.entity.TaskUserPk;
import com.muscat.Collabus.Task.repository.TaskRepository;
import com.muscat.Collabus.Task.repository.TaskUserRepository;
import com.muscat.Collabus.Todo.entity.Todo;
import com.muscat.Collabus.Todo.entity.TodoComment;
import com.muscat.Collabus.Todo.entity.TodoWork;
import com.muscat.Collabus.Todo.repository.TodoCommentRepository;
import com.muscat.Collabus.Todo.repository.TodoRepository;
import com.muscat.Collabus.Todo.repository.TodoWorkRepository;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Task 조회·댓글·작업 내용의 권한 규칙을 시큐리티 필터부터 JPA 까지 통과시켜 확인한다.
 * 서비스 단위 테스트는 권한 유틸을 대역으로 바꾸므로 실제 역할 조회까지는 확인하지 못한다.
 * 로그인 자체는 AuthenticationFlowIntegrationTest 가 덮으므로 여기서는 토큰을 바로 발급한다.
 * 트랜잭션을 롤백하므로 AFTER_COMMIT 리스너는 돌지 않는다. 알림 전송은 여기서 확인하지 못한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Task 권한 통합 테스트")
class TaskPermissionIntegrationTest {

  /** Task 참여자는 PARTICIPANT 한 명뿐이고, MEMBER 는 워크스페이스에만 있다 */
  private enum Actor {
    MASTER, MANAGER, PARTICIPANT, MEMBER, OUTSIDER
  }

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

  @Autowired
  private TodoCommentRepository todoCommentRepository;

  @Autowired
  private TodoWorkRepository todoWorkRepository;

  @MockBean
  private RefreshTokenService refreshTokenService;

  private final Map<Actor, User> users = new EnumMap<>(Actor.class);

  private Workspace workspace;
  private Task task;
  private Long todoId;

  @BeforeEach
  void setUp() {
    when(refreshTokenService.isBlacklisted(anyString())).thenReturn(false);

    for (Actor actor : Actor.values()) {
      users.put(actor, createUser(actor));
    }

    workspace = workspaceRepository.save(Workspace.builder()
        .workspaceName("권한 확인용")
        .description("권한 규칙 확인")
        .founder(users.get(Actor.MASTER))
        .build());

    join(Actor.MASTER, WorkspaceRole.MASTER);
    join(Actor.MANAGER, WorkspaceRole.MANAGER);
    join(Actor.PARTICIPANT, WorkspaceRole.MEMBER);
    join(Actor.MEMBER, WorkspaceRole.MEMBER);

    User participant = users.get(Actor.PARTICIPANT);
    task = taskRepository.save(Task.builder()
        .workspace(workspace)
        .title("참여자만 있는 Task")
        .dueDate(LocalDate.now().plusDays(7))
        .taskManager(participant)
        .build());
    taskUserRepository.save(TaskUser.builder()
        .id(new TaskUserPk(task.getId(), participant.getId()))
        .task(task)
        .user(participant)
        .role(TaskRole.MANAGER)
        .build());

    todoId = todoRepository.save(Todo.builder()
        .task(task)
        .title("할 일")
        .status(TodoStatus.IN_PROGRESS)
        .dueDate(LocalDate.now().plusDays(3))
        .assignee(participant)
        .build()).getId();
  }

  private User createUser(Actor actor) {
    String nickname = actor.name().toLowerCase();
    String tag = String.valueOf(1000 + actor.ordinal());
    return userRepository.save(User.builder()
        .email(nickname + "@test.com")
        .nickname(nickname)
        .password("로그인을 하지 않으므로 쓰이지 않는다")
        .tag(tag)
        .displayName(nickname + "#" + tag)
        .role(SystemRole.USER)
        .build());
  }

  private void join(Actor actor, WorkspaceRole role) {
    User user = users.get(actor);
    workspaceUserRepository.save(WorkspaceUser.builder()
        .id(new WorkspaceUserPk(workspace.getId(), user.getId()))
        .workspace(workspace)
        .user(user)
        .role(role)
        .build());
  }

  private String bearer(Actor actor) {
    User user = users.get(actor);
    return "Bearer " + jwtUtil.generateToken(
        user.getId(), user.getEmail(), user.getRole().name(), user.getDisplayName());
  }

  private Long commentByParticipant() {
    return todoCommentRepository.save(TodoComment.builder()
        .todo(todoRepository.findById(todoId).orElseThrow())
        .author(users.get(Actor.PARTICIPANT))
        .content("원래 내용")
        .build()).getId();
  }

  private Long workByParticipant() {
    return todoWorkRepository.save(TodoWork.builder()
        .todo(todoRepository.findById(todoId).orElseThrow())
        .author(users.get(Actor.PARTICIPANT))
        .title("원래 작업")
        .content("원래 내용")
        .build()).getId();
  }

  @ParameterizedTest(name = "{0} 가 Task 를 조회하면 {1}")
  @CsvSource({
      "MASTER, 200",
      "MANAGER, 200",
      "PARTICIPANT, 200",
      "MEMBER, 403",
      "OUTSIDER, 403",
  })
  @DisplayName("Task 조회는 워크스페이스 MASTER·MANAGER 와 참여자에게만 열린다")
  void getTask(Actor actor, int expectedStatus) throws Exception {
    mockMvc.perform(get("/api/tasks/" + task.getId())
            .header("Authorization", bearer(actor)))
        .andExpect(status().is(expectedStatus));
  }

  @ParameterizedTest(name = "{0} 가 댓글을 쓰면 {1}")
  @CsvSource({
      "MASTER, 200",
      "MANAGER, 200",
      "PARTICIPANT, 200",
      "MEMBER, 403",
      "OUTSIDER, 403",
  })
  @DisplayName("댓글은 Task 를 볼 수 있는 사람이면 쓸 수 있다")
  void addComment(Actor actor, int expectedStatus) throws Exception {
    mockMvc.perform(post("/api/todo/comments")
            .param("todoId", String.valueOf(todoId))
            .header("Authorization", bearer(actor))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"content":"확인했습니다"}
                """))
        .andExpect(status().is(expectedStatus));

    List<TodoComment> saved =
        todoCommentRepository.findAllByTodoId(todoId, Pageable.unpaged()).getContent();
    if (expectedStatus == 200) {
      assertThat(saved).hasSize(1);
      assertThat(saved.get(0).getAuthor().getId()).isEqualTo(users.get(actor).getId());
    } else {
      assertThat(saved).isEmpty();
    }
  }

  @ParameterizedTest(name = "{0} 가 작업 내용을 쓰면 {1}")
  @CsvSource({
      "MASTER, 200",
      "MANAGER, 200",
      "PARTICIPANT, 200",
      "MEMBER, 403",
      "OUTSIDER, 403",
  })
  @DisplayName("작업 내용도 Task 를 볼 수 있는 사람이면 쓸 수 있다")
  void createWork(Actor actor, int expectedStatus) throws Exception {
    mockMvc.perform(post("/api/todo/works")
            .param("todoId", String.valueOf(todoId))
            .header("Authorization", bearer(actor))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"작업","content":"진행 내용"}
                """))
        .andExpect(status().is(expectedStatus));

    List<TodoWork> saved = todoWorkRepository.findAllByTodoId(todoId);
    if (expectedStatus == 200) {
      assertThat(saved).hasSize(1);
      assertThat(saved.get(0).getAuthor().getId()).isEqualTo(users.get(actor).getId());
    } else {
      assertThat(saved).isEmpty();
    }
  }

  @Test
  @DisplayName("막힌 응답은 시큐리티 기본 403 이 아니라 Task 조회 권한 메시지를 담는다")
  void getTask_DeniedMessage() throws Exception {
    mockMvc.perform(get("/api/tasks/" + task.getId())
            .header("Authorization", bearer(Actor.MEMBER)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("Task 를 볼 권한이 없습니다."));
  }

  @Test
  @DisplayName("토큰 없이 댓글을 쓰면 401")
  void addComment_NoToken() throws Exception {
    mockMvc.perform(post("/api/todo/comments")
            .param("todoId", String.valueOf(todoId))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"content":"확인했습니다"}
                """))
        .andExpect(status().isUnauthorized());
  }

  @ParameterizedTest(name = "{0} 가 PARTICIPANT 의 댓글을 수정하면 {1}")
  @CsvSource({
      "PARTICIPANT, 200",
      "MASTER, 403",
      "MANAGER, 403",
      "MEMBER, 403",
  })
  @DisplayName("댓글 수정은 넓히지 않았다. 작성자 본인만 고칠 수 있다")
  void updateComment(Actor actor, int expectedStatus) throws Exception {
    Long commentId = commentByParticipant();

    mockMvc.perform(patch("/api/todo/comments/" + commentId)
            .header("Authorization", bearer(actor))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"content":"고친 내용"}
                """))
        .andExpect(status().is(expectedStatus));

    String content = todoCommentRepository.findById(commentId).orElseThrow().getContent();
    assertThat(content).isEqualTo(expectedStatus == 200 ? "고친 내용" : "원래 내용");
  }

  @ParameterizedTest(name = "{0} 가 PARTICIPANT 의 댓글을 지우면 {1}")
  @CsvSource({
      "PARTICIPANT, 200",
      "MASTER, 403",
      "MANAGER, 403",
      "MEMBER, 403",
  })
  @DisplayName("댓글 삭제도 작성자 본인만 할 수 있다")
  void deleteComment(Actor actor, int expectedStatus) throws Exception {
    Long commentId = commentByParticipant();

    mockMvc.perform(delete("/api/todo/comments/" + commentId)
            .header("Authorization", bearer(actor)))
        .andExpect(status().is(expectedStatus));

    assertThat(todoCommentRepository.findById(commentId).isPresent())
        .isEqualTo(expectedStatus != 200);
  }

  @ParameterizedTest(name = "{0} 가 PARTICIPANT 의 작업 내용을 지우면 {1}")
  @CsvSource({
      "PARTICIPANT, 200",
      "MASTER, 403",
      "MANAGER, 403",
      "MEMBER, 403",
  })
  @DisplayName("작업 내용 삭제도 작성자 본인만 할 수 있다")
  void deleteWork(Actor actor, int expectedStatus) throws Exception {
    Long workId = workByParticipant();

    mockMvc.perform(delete("/api/todo/works/" + workId)
            .header("Authorization", bearer(actor)))
        .andExpect(status().is(expectedStatus));

    assertThat(todoWorkRepository.findById(workId).isPresent()).isEqualTo(expectedStatus != 200);
  }
}
