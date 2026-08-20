package com.muscat.Collabus.Todo.service.impl;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.muscat.Collabus.Notification.service.NotificationService;
import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Task.entity.TaskUser;
import com.muscat.Collabus.Task.repository.TaskUserRepository;
import com.muscat.Collabus.Todo.entity.Todo;
import com.muscat.Collabus.Todo.entity.TodoComment;
import com.muscat.Collabus.Todo.mapper.TodoCommentMapper;
import com.muscat.Collabus.Todo.model.TodoCommentDto;
import com.muscat.Collabus.Todo.repository.TodoCommentRepository;
import com.muscat.Collabus.Todo.repository.TodoRepository;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.Workspace.entity.Workspace;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.common.util.EntityFinderUtil;
import com.muscat.Collabus.common.util.SortGuard;
import com.muscat.Collabus.common.util.TaskAuthorityUtil;
import com.muscat.Collabus.enums.NotificationType;
import com.muscat.Collabus.enums.response.CommonResponse;
import com.muscat.Collabus.enums.role.TaskRole;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("댓글")
class TodoCommentServiceImplTest {

  private static final Long TODO_ID = 30L;
  private static final Long AUTHOR_ID = 1L;
  private static final Long ASSIGNEE_ID = 2L;
  private static final Long MANAGER_ID = 3L;
  private static final Long OUTSIDER_ID = 99L;
  private static final Long COMMENT_ID = 40L;

  @Mock
  private EntityFinderUtil finder;

  @Mock
  private TaskAuthorityUtil taskAuthorityUtil;

  @Mock
  private SortGuard sortGuard;

  @Mock
  private TodoRepository todoRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private TodoCommentRepository commentRepository;

  @Mock
  private TodoCommentMapper commentMapper;

  @Mock
  private NotificationService notificationService;

  @Mock
  private TaskUserRepository taskUserRepository;

  @InjectMocks
  private TodoCommentServiceImpl commentService;

  private Task task;
  private User author;

  @BeforeEach
  void setUp() {
    task = Task.builder().id(20L).workspace(Workspace.builder().id(10L).build()).build();
    author = User.builder().id(AUTHOR_ID).build();
  }

  private Todo todoWithAssignee(User assignee) {
    return Todo.builder().id(TODO_ID).title("todo").task(task).assignee(assignee).build();
  }

  private void writable(Todo todo) {
    when(todoRepository.findById(TODO_ID)).thenReturn(Optional.of(todo));
    when(userRepository.findById(AUTHOR_ID)).thenReturn(Optional.of(author));
    when(commentRepository.save(any(TodoComment.class))).thenAnswer(i -> i.getArgument(0));
    when(commentMapper.mapToDto(any())).thenReturn(TodoCommentDto.builder().build());
  }

  @Test
  @DisplayName("볼 수 없는 Task 의 Todo 에는 댓글을 쓸 수 없다")
  void addComment_Fail_CannotView() {
    Todo todo = todoWithAssignee(null);
    when(todoRepository.findById(TODO_ID)).thenReturn(Optional.of(todo));
    doThrow(new BusinessException(CommonResponse.TASK_VIEW_DENIED))
        .when(taskAuthorityUtil).validateCanViewTask(task, OUTSIDER_ID);

    assertThatThrownBy(() -> commentService.addComment(TODO_ID, "hi", OUTSIDER_ID))
        .isInstanceOf(BusinessException.class);

    verify(commentRepository, never()).save(any());
  }

  @Test
  @DisplayName("없는 Todo 에는 댓글을 쓸 수 없다")
  void addComment_Fail_TodoNotFound() {
    when(todoRepository.findById(TODO_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> commentService.addComment(TODO_ID, "hi", AUTHOR_ID))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("담당자와 Task MANAGER 전원에게 알림이 간다")
  void addComment_NotifiesAssigneeAndManagers() {
    Todo todo = todoWithAssignee(User.builder().id(ASSIGNEE_ID).build());
    writable(todo);
    when(taskUserRepository.findByTaskAndRole(task, TaskRole.MANAGER)).thenReturn(List.of(
        TaskUser.builder().user(User.builder().id(MANAGER_ID).build()).build()));

    commentService.addComment(TODO_ID, "hi", AUTHOR_ID);

    verify(notificationService, times(1))
        .createNotification(eq(ASSIGNEE_ID), eq(NotificationType.COMMENT_ADDED), anyString(),
            eq(TODO_ID));
    verify(notificationService, times(1))
        .createNotification(eq(MANAGER_ID), eq(NotificationType.COMMENT_ADDED), anyString(),
            eq(TODO_ID));
  }

  @Test
  @DisplayName("담당자가 없으면 MANAGER 에게만 알림이 간다")
  void addComment_NoAssignee() {
    Todo todo = todoWithAssignee(null);
    writable(todo);
    when(taskUserRepository.findByTaskAndRole(task, TaskRole.MANAGER)).thenReturn(List.of(
        TaskUser.builder().user(User.builder().id(MANAGER_ID).build()).build()));

    commentService.addComment(TODO_ID, "hi", AUTHOR_ID);

    verify(notificationService, times(1))
        .createNotification(eq(MANAGER_ID), any(), anyString(), anyLong());
    verify(notificationService, times(1))
        .createNotification(anyLong(), any(), anyString(), anyLong());
  }

  @Test
  @DisplayName("작성자 자신에게는 알림이 가지 않는다")
  void addComment_SkipsSelf() {
    Todo todo = todoWithAssignee(author);
    writable(todo);
    when(taskUserRepository.findByTaskAndRole(task, TaskRole.MANAGER)).thenReturn(List.of(
        TaskUser.builder().user(author).build()));

    commentService.addComment(TODO_ID, "hi", AUTHOR_ID);

    verify(notificationService, never())
        .createNotification(anyLong(), any(), anyString(), anyLong());
  }

  @Test
  @DisplayName("작성자 본인만 수정할 수 있다")
  void updateComment_Fail_NotAuthor() {
    TodoComment comment = TodoComment.builder().author(author).content("old").build();
    when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(comment));

    assertThatThrownBy(() -> commentService.updateComment(COMMENT_ID, "new", OUTSIDER_ID))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("작성자는 댓글을 수정할 수 있다")
  void updateComment_Success() {
    TodoComment comment = TodoComment.builder().author(author).content("old").build();
    when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(comment));
    when(commentRepository.save(comment)).thenReturn(comment);
    when(commentMapper.mapToDto(comment)).thenReturn(TodoCommentDto.builder().build());

    assertThatCode(() -> commentService.updateComment(COMMENT_ID, "new", AUTHOR_ID))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("작성자 본인만 삭제할 수 있다")
  void deleteComment_Fail_NotAuthor() {
    TodoComment comment = TodoComment.builder().author(author).build();
    when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(comment));

    assertThatThrownBy(() -> commentService.deleteComment(COMMENT_ID, OUTSIDER_ID))
        .isInstanceOf(BusinessException.class);

    verify(commentRepository, never()).delete(any());
  }

  @Test
  @DisplayName("없는 댓글은 수정할 수 없다")
  void updateComment_Fail_NotFound() {
    when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> commentService.updateComment(COMMENT_ID, "new", AUTHOR_ID))
        .isInstanceOf(BusinessException.class);
  }
}
