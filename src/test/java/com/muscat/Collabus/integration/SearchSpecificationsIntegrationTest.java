package com.muscat.Collabus.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Task.repository.TaskRepository;
import com.muscat.Collabus.Todo.entity.Todo;
import com.muscat.Collabus.Todo.repository.TodoRepository;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.Workspace.entity.Workspace;
import com.muscat.Collabus.Workspace.repository.WorkspaceRepository;
import com.muscat.Collabus.common.util.TaskSpecifications;
import com.muscat.Collabus.common.util.TodoSpecifications;
import com.muscat.Collabus.enums.role.SystemRole;
import com.muscat.Collabus.enums.status.TodoStatus;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

/**
 * 제목 · 설명 검색 조건을 실제 쿼리로 확인한다.
 */
@SpringBootTest
@Transactional
@DisplayName("검색 조건 통합 테스트")
class SearchSpecificationsIntegrationTest {

  private static final LocalDate DUE = LocalDate.now().plusDays(7);

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private WorkspaceRepository workspaceRepository;

  @Autowired
  private TaskRepository taskRepository;

  @Autowired
  private TodoRepository todoRepository;

  private User owner;
  private Workspace workspace;
  private Task task;

  @BeforeEach
  void setUp() {
    owner = userRepository.save(User.builder()
        .email("search@test.com")
        .nickname("search")
        .password("로그인을 하지 않으므로 쓰이지 않는다")
        .tag("0001")
        .displayName("search#0001")
        .role(SystemRole.USER)
        .build());
    workspace = workspaceRepository.save(Workspace.builder()
        .workspaceName("검색 확인용")
        .description("검색")
        .founder(owner)
        .build());
    task = saveTask("캠페인 준비", null);

    saveTodo("UI 시안 정리", null, TodoStatus.IN_PROGRESS);
    saveTodo("배너 문구", "메인 시안에 들어갈 문구", TodoStatus.WAITING_REVIEW);
    saveTodo("할인 50% 배너", "기간 한정", TodoStatus.IN_PROGRESS);
    saveTodo("할인 배너", "50 개 제작", TodoStatus.IN_PROGRESS);
    saveTodo("file_name 규칙", null, TodoStatus.CONFIRMED);
    saveTodo("filename 정리", null, TodoStatus.CONFIRMED);
  }

  private Task saveTask(String title, String description) {
    return taskRepository.save(Task.builder()
        .workspace(workspace)
        .title(title)
        .description(description)
        .dueDate(DUE)
        .taskManager(owner)
        .build());
  }

  private void saveTodo(String title, String description, TodoStatus status) {
    todoRepository.save(Todo.builder()
        .task(task)
        .title(title)
        .description(description)
        .status(status)
        .dueDate(DUE)
        .assignee(owner)
        .build());
  }

  private List<String> todoTitles(Specification<Todo> spec) {
    return todoRepository.findAll(TodoSpecifications.inTask(task.getId()).and(spec),
            PageRequest.of(0, 20)).getContent().stream()
        .map(Todo::getTitle)
        .sorted()
        .toList();
  }

  @Test
  @DisplayName("Todo 는 대소문자를 가리지 않고 제목에서 찾는다")
  void todoTitleIgnoresCase() {
    assertThat(todoTitles(TodoSpecifications.matches("ui"))).containsExactly("UI 시안 정리");
  }

  @Test
  @DisplayName("Todo 는 설명에서도 찾고 설명이 없는 Todo 도 제목으로 걸린다")
  void todoDescriptionAndNullDescription() {
    assertThat(todoTitles(TodoSpecifications.matches("시안")))
        .containsExactly("UI 시안 정리", "배너 문구");
  }

  @Test
  @DisplayName("검색어의 % 와 _ 는 글자 그대로 찾는다")
  void todoMetaCharactersAreLiteral() {
    assertThat(todoTitles(TodoSpecifications.matches("50%"))).containsExactly("할인 50% 배너");
    assertThat(todoTitles(TodoSpecifications.matches("file_"))).containsExactly("file_name 규칙");
  }

  @Test
  @DisplayName("상태 조건과 함께 쓰면 둘 다 맞는 Todo 만 남는다")
  void todoKeywordWithStatus() {
    assertThat(todoTitles(TodoSpecifications.matches("배너")
        .and(TodoSpecifications.hasStatus(TodoStatus.IN_PROGRESS))))
        .containsExactly("할인 50% 배너", "할인 배너");
  }

  @Test
  @DisplayName("Task 도 같은 규칙으로 제목 · 설명에서 찾는다")
  void taskMatches() {
    saveTask("리뷰 100% 반영", "피드백 정리");
    saveTask("리뷰 반영", "100 건");

    List<String> titles = taskRepository.findAll(
            TaskSpecifications.inWorkspace(workspace.getId()).and(TaskSpecifications.matches("100%")),
            PageRequest.of(0, 20)).getContent().stream()
        .map(Task::getTitle)
        .toList();

    assertThat(titles).containsExactly("리뷰 100% 반영");
  }
}
