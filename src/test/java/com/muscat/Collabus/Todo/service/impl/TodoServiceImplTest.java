package com.muscat.Collabus.Todo.service.impl;

import static org.mockito.Mockito.lenient;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.security.access.AccessDeniedException;
import com.muscat.Collabus.common.util.SortGuard;
import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Todo.entity.Todo;
import com.muscat.Collabus.Todo.mapper.TodoMapper;
import com.muscat.Collabus.Todo.model.TodoRequestDto;
import com.muscat.Collabus.Todo.model.TodoResponseDto;
import com.muscat.Collabus.Notification.service.NotificationService;
import com.muscat.Collabus.Task.repository.TaskUserRepository;
import com.muscat.Collabus.Todo.repository.TodoCommentRepository;
import com.muscat.Collabus.Todo.repository.TodoFileRepository;
import com.muscat.Collabus.Todo.repository.TodoRepository;
import com.muscat.Collabus.Todo.repository.TodoWorkRepository;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.Workspace.entity.Workspace;
import com.muscat.Collabus.common.dto.PageResponseDto;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.enums.response.CommonResponse;
import com.muscat.Collabus.common.exception.ResourceNotFoundException;
import com.muscat.Collabus.common.util.EntityFinderUtil;
import com.muscat.Collabus.common.util.ParticipantUtil;
import com.muscat.Collabus.common.util.TaskAuthorityUtil;
import com.muscat.Collabus.enums.role.SystemRole;
import com.muscat.Collabus.enums.status.TodoStatus;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TodoService 단위 테스트")
class TodoServiceImplTest {

    @Mock

    private SortGuard sortGuard;


    @Mock
    private TodoRepository todoRepository;

    @Mock
    private TodoMapper todoMapper;

    @Mock
    private ParticipantUtil participantUtil;

    @Mock
    private TaskAuthorityUtil taskAuthorityUtil;

    @Mock
    private EntityFinderUtil finder;

    @Mock
    private TodoWorkRepository todoWorkRepository;

    @Mock
    private TodoCommentRepository todoCommentRepository;

    @Mock
    private TodoFileRepository todoFileRepository;

    @Mock
    private TaskUserRepository taskUserRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TodoServiceImpl todoService;

    private User user;
    private Workspace workspace;
    private Task task;
    private Todo todo;
    private TodoRequestDto todoRequestDto;
    private TodoResponseDto todoResponseDto;

    @BeforeEach
    void setUp() {

        // 정렬 검증은 SortGuard 가 맡는다. 여기서는 그대로 통과시킨다
        lenient().when(sortGuard.apply(any(Pageable.class), any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        user = User.builder()
                .id(1L)
                .email("user@example.com")
                .nickname("testuser")
                .password("encodedPassword")
                .tag("1234")
                .displayName("testuser#1234")
                .role(SystemRole.USER)
                .build();

        workspace = Workspace.builder()
                .id(1L)
                .workspaceName("Test Workspace")
                .description("Test Description")
                .founder(user)
                .build();

        task = Task.builder()
                .id(1L)
                .workspace(workspace)
                .taskManager(user)
                .title("Test Task")
                .description("Test Description")
                .dueDate(LocalDate.now().plusDays(30))
                .build();

        todo = Todo.builder()
                .id(1L)
                .task(task)
                .assignee(user)
                .title("Test Todo")
                .description("Test Description")
                .dueDate(LocalDate.now().plusDays(7))
                .status(TodoStatus.IN_PROGRESS)
                .build();

        todoRequestDto = TodoRequestDto.builder()
                .taskId(1L)
                .assigneeId(1L)
                .title("Test Todo")
                .description("Test Description")
                .dueDate(LocalDate.now().plusDays(7))
                .build();

        todoResponseDto = TodoResponseDto.builder()
                .id(1L)
                .title("Test Todo")
                .description("Test Description")
                .taskId(1L)
                .assigneeDisplayName("testuser#1234")
                .status(TodoStatus.IN_PROGRESS.name())  // String으로 변환
                .isDone(false)
                .build();
    }

    @Test
    @DisplayName("Todo 생성 성공")
    void createTodo_Success() {
        // Given
        Long creatorId = 1L;
        when(finder.findTaskById(todoRequestDto.getTaskId())).thenReturn(task);
        when(finder.findUserById(todoRequestDto.getAssigneeId())).thenReturn(user);
        when(todoMapper.mapToEntity(any(), any(), any())).thenReturn(todo);
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);
        when(todoMapper.mapToDto(todo)).thenReturn(todoResponseDto);

        // When
        TodoResponseDto result = todoService.createTodo(todoRequestDto, creatorId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Todo");
        verify(finder, times(1)).findTaskById(todoRequestDto.getTaskId());
        verify(taskAuthorityUtil, times(1)).validateCanManageTask(task, creatorId);
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    @DisplayName("Todo 생성 실패 - 마감일이 Task 마감일 이후")
    void createTodo_Fail_DueDateAfterTaskDueDate() {
        // Given
        Long creatorId = 1L;
        TodoRequestDto invalidDto = TodoRequestDto.builder()
                .taskId(1L)
                .assigneeId(1L)
                .title("Test Todo")
                .description("Test Description")
                .dueDate(LocalDate.now().plusDays(40)) // Task 마감일(30일) 이후
                .build();

        when(finder.findTaskById(invalidDto.getTaskId())).thenReturn(task);

        // When & Then
        assertThatThrownBy(() -> todoService.createTodo(invalidDto, creatorId))
                .isInstanceOf(BusinessException.class);

        verify(todoRepository, times(0)).save(any(Todo.class));
    }

    @Test
    @DisplayName("Todo 생성 실패 - 마감일이 현재보다 이전")
    void createTodo_Fail_DueDateBeforeNow() {
        // Given
        Long creatorId = 1L;
        TodoRequestDto invalidDto = TodoRequestDto.builder()
                .taskId(1L)
                .assigneeId(1L)
                .title("Test Todo")
                .description("Test Description")
                .dueDate(LocalDate.now().minusDays(1)) // 과거 날짜
                .build();

        when(finder.findTaskById(invalidDto.getTaskId())).thenReturn(task);

        // When & Then
        assertThatThrownBy(() -> todoService.createTodo(invalidDto, creatorId))
                .isInstanceOf(BusinessException.class);

        verify(todoRepository, times(0)).save(any(Todo.class));
    }

    @Test
    @DisplayName("Todo 수정 성공")
    void updateTodo_Success() {
        // Given
        Long todoId = 1L;
        Long updaterId = 1L;
        when(finder.findTodoById(todoId)).thenReturn(todo);
        when(taskAuthorityUtil.canManageTask(task, updaterId)).thenReturn(true);
        when(todoMapper.mapToDto(todo)).thenReturn(todoResponseDto);

        // When
        TodoResponseDto result = todoService.updateTodo(todoId, todoRequestDto, updaterId);

        // Then
        assertThat(result).isNotNull();
        verify(finder, times(1)).findTodoById(todoId);
        verify(todoMapper, times(1)).updateFromDto(todoRequestDto, todo);
    }

    @Test
    @DisplayName("Todo 수정 실패 - Manager 권한 없음")
    void updateTodo_Fail_NotManager() {
        // Given
        Long todoId = 1L;
        Long updaterId = 2L;
        when(finder.findTodoById(todoId)).thenReturn(todo);
        when(taskAuthorityUtil.canManageTask(task, updaterId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> todoService.updateTodo(todoId, todoRequestDto, updaterId))
                .isInstanceOf(BusinessException.class);

        verify(todoRepository, times(0)).save(any(Todo.class));
    }

    @Test
    @DisplayName("Todo 삭제 성공")
    void deleteTodo_Success() {
        // Given
        Long todoId = 1L;
        Long userId = 1L;
        when(finder.findTodoById(todoId)).thenReturn(todo);
        when(taskAuthorityUtil.canManageTask(task, userId)).thenReturn(true);

        // When
        todoService.deleteTodo(todoId, userId);

        // Then
        verify(finder, times(1)).findTodoById(todoId);
        verify(todoRepository, times(1)).delete(todo);
    }

    @Test
    @DisplayName("Todo 삭제 실패 - Manager 권한 없음")
    void deleteTodo_Fail_NotManager() {
        // Given
        Long todoId = 1L;
        Long userId = 2L;
        when(finder.findTodoById(todoId)).thenReturn(todo);
        when(taskAuthorityUtil.canManageTask(task, userId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> todoService.deleteTodo(todoId, userId))
                .isInstanceOf(BusinessException.class);

        verify(todoRepository, times(0)).delete(any(Todo.class));
    }

    @Test
    @DisplayName("볼 수 없는 Task 의 Todo 는 단건 조회가 막힌다")
    void getTodoById_Fail_CannotViewTask() {
        when(finder.findTodoById(1L)).thenReturn(todo);
        doThrow(new AccessDeniedException("Task 를 볼 권한이 없습니다."))
                .when(taskAuthorityUtil).validateCanViewTask(todo.getTask(), 99L);

        assertThatThrownBy(() -> todoService.getTodoById(1L, 99L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("볼 수 없는 Task 는 Todo 목록도 막힌다")
    void getTodosByTask_Fail_CannotViewTask() {
        when(finder.findTaskById(1L)).thenReturn(task);
        doThrow(new AccessDeniedException("Task 를 볼 권한이 없습니다."))
                .when(taskAuthorityUtil).validateCanViewTask(task, 99L);

        assertThatThrownBy(() ->
                todoService.getTodosByTask(1L, 99L, null, PageRequest.of(0, 20)))
                .isInstanceOf(AccessDeniedException.class);

        verify(todoRepository, times(0)).findAllByTaskId(any(), any());
    }

    @Test
    @DisplayName("Todo ID로 조회 성공")
    void getTodoById_Success() {
        // Given
        Long todoId = 1L;
        when(finder.findTodoById(todoId)).thenReturn(todo);
        when(todoMapper.mapToDto(todo)).thenReturn(todoResponseDto);

        // When
        TodoResponseDto result = todoService.getTodoById(todoId, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(todoId);
        verify(finder, times(1)).findTodoById(todoId);
    }

    @Test
    @DisplayName("Todo 조회 실패 - 존재하지 않음")
    void getTodoById_Fail_NotFound() {
        // Given
        Long todoId = 999L;
        when(finder.findTodoById(todoId)).thenThrow(new ResourceNotFoundException(CommonResponse.RESOURCE_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> todoService.getTodoById(todoId, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Task별 Todo 목록 조회 성공 - 상태 필터 없음")
    void getTodosByTask_Success_WithoutStatus() {
        // Given
        Long taskId = 1L;
        Pageable pageable = PageRequest.of(0, 20);
        when(todoRepository.findAllByTaskId(taskId, pageable))
                .thenReturn(new PageImpl<>(List.of(todo), pageable, 1));
        when(todoMapper.mapToDto(todo)).thenReturn(todoResponseDto);

        // When
        PageResponseDto<TodoResponseDto> result = todoService.getTodosByTask(taskId, 1L, null, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Todo");
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(todoRepository, times(1)).findAllByTaskId(taskId, pageable);
    }

    @Test
    @DisplayName("Task별 Todo 목록 조회 성공 - 상태 필터 있음")
    void getTodosByTask_Success_WithStatus() {
        // Given
        Long taskId = 1L;
        String status = "IN_PROGRESS";
        Pageable pageable = PageRequest.of(0, 20);
        when(todoRepository.findAllByTaskIdAndStatus(taskId, TodoStatus.IN_PROGRESS, pageable))
                .thenReturn(new PageImpl<>(List.of(todo), pageable, 1));
        when(todoMapper.mapToDto(todo)).thenReturn(todoResponseDto);

        // When
        PageResponseDto<TodoResponseDto> result = todoService.getTodosByTask(taskId, 1L, status, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        verify(todoRepository, times(1))
                .findAllByTaskIdAndStatus(taskId, TodoStatus.IN_PROGRESS, pageable);
    }

    @Test
    @DisplayName("Task별 Todo 목록 조회 실패 - 알 수 없는 상태값")
    void getTodosByTask_Fail_InvalidStatus() {
        // When & Then
        assertThatThrownBy(() -> todoService.getTodosByTask(1L, 1L, "BOGUS", PageRequest.of(0, 20)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("자신의 Todo 완료 처리 성공")
    void completeOwnTodo_Success() {
        // Given
        Long todoId = 1L;
        Long userId = 1L;
        when(finder.findTodoById(todoId)).thenReturn(todo);

        // When
        todoService.completeOwnTodo(todoId, userId);

        // Then
        verify(finder, times(1)).findTodoById(todoId);
        verify(participantUtil, times(1)).validateTaskParticipant(task.getId(), userId);
        assertThat(todo.getStatus()).isEqualTo(TodoStatus.WAITING_REVIEW);
        assertThat(todo.getDoneAt()).isNotNull();
    }

    @Test
    @DisplayName("자신의 Todo 완료 처리 실패 - 담당자가 아님")
    void completeOwnTodo_Fail_NotAssignee() {
        // Given
        Long todoId = 1L;
        Long userId = 2L; // 다른 사용자
        when(finder.findTodoById(todoId)).thenReturn(todo);

        // When & Then
        assertThatThrownBy(() -> todoService.completeOwnTodo(todoId, userId))
                .isInstanceOf(BusinessException.class);

        verify(todoRepository, times(0)).save(any(Todo.class));
    }

    @Test
    @DisplayName("Todo 완료 최종 확인 성공")
    void confirmTodoCompletion_Success() {
        // Given
        Long todoId = 1L;
        Long taskManagerId = 1L;
        todo.requestReview();

        when(finder.findTodoById(todoId)).thenReturn(todo);
        when(todoMapper.mapToDto(todo)).thenReturn(todoResponseDto);

        // When
        TodoResponseDto result = todoService.confirmTodoCompletion(todoId, taskManagerId);

        // Then
        assertThat(result).isNotNull();
        verify(finder, times(1)).findTodoById(todoId);
        verify(taskAuthorityUtil, times(1)).validateTaskManager(task, taskManagerId);
        assertThat(todo.getStatus()).isEqualTo(TodoStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Todo 완료 최종 확인 실패 - WAITING_REVIEW 상태가 아님")
    void confirmTodoCompletion_Fail_NotWaitingReview() {
        // Given
        Long todoId = 1L;
        Long taskManagerId = 1L;

        when(finder.findTodoById(todoId)).thenReturn(todo);

        // When & Then
        assertThatThrownBy(() -> todoService.confirmTodoCompletion(todoId, taskManagerId))
                .isInstanceOf(BusinessException.class);

        verify(todoRepository, times(0)).save(any(Todo.class));
    }

    @Test
    @DisplayName("Todo 담당자 변경 성공")
    void changeAssignee_Success() {
        // Given
        Long todoId = 1L;
        Long newAssigneeId = 2L;
        Long managerId = 1L;
        User newAssignee = User.builder().id(2L).build();

        when(finder.findTodoById(todoId)).thenReturn(todo);
        when(finder.findUserById(newAssigneeId)).thenReturn(newAssignee);

        // When
        todoService.changeAssignee(todoId, newAssigneeId, managerId);

        // Then
        verify(finder, times(1)).findTodoById(todoId);
        verify(taskAuthorityUtil, times(1)).validateTaskManager(task, managerId);
        verify(participantUtil, times(1)).validateTaskParticipant(task.getId(), newAssigneeId);
        verify(finder, times(1)).findUserById(newAssigneeId);
        assertThat(todo.getAssignee()).isEqualTo(newAssignee);
    }

    @Test
    @DisplayName("Todo 담당자 변경 실패 - Manager 권한 없음")
    void changeAssignee_Fail_NotManager() {
        // Given
        Long todoId = 1L;
        Long newAssigneeId = 2L;
        Long managerId = 2L;

        when(finder.findTodoById(todoId)).thenReturn(todo);
        doThrow(new BusinessException(CommonResponse.FORBIDDEN))
                .when(taskAuthorityUtil).validateTaskManager(task, managerId);

        // When & Then
        assertThatThrownBy(() -> todoService.changeAssignee(todoId, newAssigneeId, managerId))
                .isInstanceOf(BusinessException.class);

        verify(todoRepository, times(0)).save(any(Todo.class));
    }

    @Test
    @DisplayName("Todo 담당자 변경 실패 - 동일한 담당자")
    void changeAssignee_Fail_SameAssignee() {
        // Given
        Long todoId = 1L;
        Long sameAssigneeId = 1L; // 현재 담당자와 동일
        Long managerId = 1L;

        when(finder.findTodoById(todoId)).thenReturn(todo);

        // When & Then
        assertThatThrownBy(() -> todoService.changeAssignee(todoId, sameAssigneeId, managerId))
                .isInstanceOf(BusinessException.class);

        verify(todoRepository, times(0)).save(any(Todo.class));
    }
}
