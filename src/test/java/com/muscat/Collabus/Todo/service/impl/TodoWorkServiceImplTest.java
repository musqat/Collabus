package com.muscat.Collabus.Todo.service.impl;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Todo.entity.Todo;
import com.muscat.Collabus.Todo.entity.TodoWork;
import com.muscat.Collabus.Todo.mapper.TodoWorkMapper;
import com.muscat.Collabus.Todo.model.TodoWorkDto;
import com.muscat.Collabus.Todo.repository.TodoWorkRepository;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.Workspace.entity.Workspace;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.common.util.EntityFinderUtil;
import com.muscat.Collabus.common.util.SortGuard;
import com.muscat.Collabus.common.util.TaskAuthorityUtil;
import com.muscat.Collabus.enums.response.CommonResponse;
import com.muscat.Collabus.enums.status.TodoStatus;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("작업 내용")
class TodoWorkServiceImplTest {

  private static final Long TODO_ID = 30L;
  private static final Long WORK_ID = 40L;
  private static final Long AUTHOR_ID = 1L;
  private static final Long OUTSIDER_ID = 99L;

  @Mock
  private TaskAuthorityUtil taskAuthorityUtil;

  @Mock
  private SortGuard sortGuard;

  @Mock
  private TodoWorkMapper todoWorkMapper;

  @Mock
  private TodoWorkRepository todoWorkRepository;

  @Mock
  private EntityFinderUtil finder;

  @InjectMocks
  private TodoWorkServiceImpl todoWorkService;

  private Task task;
  private User author;

  @BeforeEach
  void setUp() {
    task = Task.builder().id(20L).workspace(Workspace.builder().id(10L).build()).build();
    author = User.builder().id(AUTHOR_ID).build();
  }

  private Todo todoWith(TodoStatus status) {
    return Todo.builder().id(TODO_ID).task(task).status(status).build();
  }

  @Test
  @DisplayName("CONFIRMED 상태의 Todo 에는 작업 내용을 만들 수 없다")
  void createWork_Fail_Confirmed() {
    when(finder.findTodoById(TODO_ID)).thenReturn(todoWith(TodoStatus.CONFIRMED));

    assertThatThrownBy(() ->
        todoWorkService.createWork(TODO_ID, TodoWorkDto.builder().build(), AUTHOR_ID))
        .isInstanceOf(BusinessException.class);

    verify(todoWorkRepository, never()).save(any());
  }

  @Test
  @DisplayName("볼 수 없는 Task 의 Todo 에는 작업 내용을 만들 수 없다")
  void createWork_Fail_CannotView() {
    Todo todo = todoWith(TodoStatus.IN_PROGRESS);
    when(finder.findTodoById(TODO_ID)).thenReturn(todo);
    doThrow(new BusinessException(CommonResponse.TASK_VIEW_DENIED))
        .when(taskAuthorityUtil).validateCanViewTask(task, OUTSIDER_ID);

    assertThatThrownBy(() ->
        todoWorkService.createWork(TODO_ID, TodoWorkDto.builder().build(), OUTSIDER_ID))
        .isInstanceOf(BusinessException.class);

    verify(todoWorkRepository, never()).save(any());
  }

  @Test
  @DisplayName("작성자 본인만 수정할 수 있다")
  void updateWork_Fail_NotAuthor() {
    TodoWork work = TodoWork.builder().id(WORK_ID).author(author).build();
    when(todoWorkRepository.findById(WORK_ID)).thenReturn(Optional.of(work));

    assertThatThrownBy(() ->
        todoWorkService.updateWork(WORK_ID, TodoWorkDto.builder().build(), OUTSIDER_ID))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("없는 작업 내용은 수정할 수 없다")
  void updateWork_Fail_NotFound() {
    when(todoWorkRepository.findById(WORK_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() ->
        todoWorkService.updateWork(WORK_ID, TodoWorkDto.builder().build(), AUTHOR_ID))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("작성자 본인만 삭제할 수 있다")
  void deleteWork_Fail_NotAuthor() {
    TodoWork work = TodoWork.builder().id(WORK_ID).author(author).build();
    when(todoWorkRepository.findById(WORK_ID)).thenReturn(Optional.of(work));

    assertThatThrownBy(() -> todoWorkService.deleteWork(WORK_ID, OUTSIDER_ID))
        .isInstanceOf(BusinessException.class);

    verify(todoWorkRepository, never()).delete(any());
  }

  @Test
  @DisplayName("작성자는 작업 내용을 지울 수 있다")
  void deleteWork_Success() {
    TodoWork work = TodoWork.builder().id(WORK_ID).author(author).build();
    when(todoWorkRepository.findById(WORK_ID)).thenReturn(Optional.of(work));

    assertThatCode(() -> todoWorkService.deleteWork(WORK_ID, AUTHOR_ID))
        .doesNotThrowAnyException();

    verify(todoWorkRepository, times(1)).delete(work);
  }
}
