package com.muscat.Collabus.Task.service.impl;

import static org.mockito.Mockito.lenient;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.muscat.Collabus.common.util.SortGuard;
import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Task.entity.TaskUser;
import com.muscat.Collabus.Task.mapper.TaskMapper;
import com.muscat.Collabus.Task.mapper.TaskUserMapper;
import com.muscat.Collabus.Task.model.TaskRequestDto;
import com.muscat.Collabus.Task.model.TaskResponseDto;
import com.muscat.Collabus.Task.model.TaskUpdateRequestDto;
import com.muscat.Collabus.Task.model.TaskUserResponseDto;
import com.muscat.Collabus.Notification.service.NotificationService;
import com.muscat.Collabus.Task.repository.TaskRepository;
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
import com.muscat.Collabus.common.util.EntityFinderUtil;
import com.muscat.Collabus.common.util.TaskAuthorityUtil;
import com.muscat.Collabus.enums.role.SystemRole;
import com.muscat.Collabus.enums.role.TaskRole;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import com.muscat.Collabus.Task.model.TodoProgressDto;
import com.muscat.Collabus.Todo.entity.Todo;
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
@DisplayName("TaskService 단위 테스트")
class TaskServiceImplTest {

    @Mock

    private SortGuard sortGuard;


    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskUserRepository taskUserRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskUserMapper taskUserMapper;

    @Mock
    private TaskAuthorityUtil taskAuthorityUtil;

    @Mock
    private EntityFinderUtil finder;

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private TodoWorkRepository todoWorkRepository;

    @Mock
    private TodoCommentRepository todoCommentRepository;

    @Mock
    private TodoFileRepository todoFileRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User user;
    private Workspace workspace;
    private Task task;
    private TaskRequestDto taskRequestDto;
    private TaskResponseDto taskResponseDto;
    private TaskUpdateRequestDto taskUpdateRequestDto;
    private TaskUser taskUser;

    @BeforeEach
    void setUp() {

        // 정렬 검증은 SortGuard 가 맡는다. 여기서는 그대로 통과시킨다
        lenient().when(sortGuard.apply(any(Pageable.class), any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(sortGuard.apply(any(Pageable.class), any()))
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
                .dueDate(LocalDate.now().plusDays(7))
                .build();

        taskRequestDto = new TaskRequestDto();
        taskRequestDto.setWorkspaceId(1L);
        taskRequestDto.setTitle("Test Task");
        taskRequestDto.setDescription("Test Description");
        taskRequestDto.setDueDate(LocalDate.now().plusDays(7));

        taskResponseDto = TaskResponseDto.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .workspaceId(1L)
                .managerDisplayName("testuser#1234")
                .build();

        taskUpdateRequestDto = TaskUpdateRequestDto.builder()
                .title("Updated Task")
                .description("Updated Description")
                .dueDate(LocalDate.now().plusDays(14))
                .build();
    }

    @Test
    @DisplayName("Task 생성 성공")
    void createTask_Success() {
        // Given
        Long userId = 1L;
        when(finder.findUserById(userId)).thenReturn(user);
        when(finder.findWorkspaceById(taskRequestDto.getWorkspaceId())).thenReturn(workspace);
        when(taskMapper.mapToEntity(any(), any(), any())).thenReturn(task);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskUserRepository.save(any(TaskUser.class))).thenReturn(taskUser);
        when(taskMapper.mapToDto(task)).thenReturn(taskResponseDto);

        // When
        TaskResponseDto result = taskService.createTask(taskRequestDto, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Task");
        verify(finder, times(1)).findUserById(userId);
        verify(finder, times(1)).findWorkspaceById(taskRequestDto.getWorkspaceId());
        verify(taskAuthorityUtil, times(1)).validateCanCreateTask(workspace, userId);
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(taskUserRepository, times(1)).save(any(TaskUser.class));
    }

    @Test
    @DisplayName("Task 생성 실패 - 권한 없음")
    void createTask_Fail_Unauthorized() {
        // Given
        Long userId = 2L;
        when(finder.findUserById(userId)).thenReturn(user);
        when(finder.findWorkspaceById(taskRequestDto.getWorkspaceId())).thenReturn(workspace);
        doThrow(new BusinessException(CommonResponse.FORBIDDEN))
                .when(taskAuthorityUtil).validateCanCreateTask(workspace, userId);

        // When & Then
        assertThatThrownBy(() -> taskService.createTask(taskRequestDto, userId))
                .isInstanceOf(BusinessException.class);

        verify(taskRepository, times(0)).save(any(Task.class));
    }

    @Test
    @DisplayName("Task 조회 성공")
    void getTask_Success() {
        // Given
        Long taskId = 1L;
        when(taskAuthorityUtil.requireViewableTask(taskId, 1L)).thenReturn(task);
        when(taskMapper.mapToDto(task)).thenReturn(taskResponseDto);

        // When
        TaskResponseDto result = taskService.getTask(taskId, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(taskId);
    }

    @Test
    @DisplayName("없는 Task 도 볼 권한이 없는 것과 같은 예외가 난다")
    void getTask_Fail_NotFound() {
        // Given
        Long taskId = 999L;
        when(taskAuthorityUtil.requireViewableTask(taskId, 1L))
                .thenThrow(new AccessDeniedException("Task 를 볼 권한이 없습니다."));

        // When & Then
        assertThatThrownBy(() -> taskService.getTask(taskId, 1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Task 수정 성공")
    void updateTask_Success() {
        // Given
        Long taskId = 1L;
        Long userId = 1L;
        when(finder.findTaskById(taskId)).thenReturn(task);
        when(taskMapper.mapToDto(task)).thenReturn(taskResponseDto);

        // When
        TaskResponseDto result = taskService.updateTask(taskId, taskUpdateRequestDto, userId);

        // Then
        assertThat(result).isNotNull();
        verify(finder, times(1)).findTaskById(taskId);
        verify(taskAuthorityUtil, times(1)).validateCanManageTask(task, userId);
    }

    @Test
    @DisplayName("Task 수정 실패 - 권한 없음")
    void updateTask_Fail_Unauthorized() {
        // Given
        Long taskId = 1L;
        Long userId = 2L;
        when(finder.findTaskById(taskId)).thenReturn(task);
        doThrow(new BusinessException(CommonResponse.FORBIDDEN))
                .when(taskAuthorityUtil).validateCanManageTask(task, userId);

        // When & Then
        assertThatThrownBy(() -> taskService.updateTask(taskId, taskUpdateRequestDto, userId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Task 삭제 성공")
    void deleteTask_Success() {
        // Given
        Long taskId = 1L;
        Long userId = 1L;
        when(finder.findTaskById(taskId)).thenReturn(task);

        // When
        taskService.deleteTask(taskId, userId);

        // Then
        verify(finder, times(1)).findTaskById(taskId);
        verify(taskAuthorityUtil, times(1)).validateCanManageTask(task, userId);
        verify(taskRepository, times(1)).delete(task);
    }

    @Test
    @DisplayName("Task 삭제 실패 - 관리 권한 없음")
    void deleteTask_Fail_Unauthorized() {
        // Given
        Long taskId = 1L;
        Long userId = 99L;
        when(finder.findTaskById(taskId)).thenReturn(task);
        doThrow(new BusinessException(CommonResponse.FORBIDDEN))
                .when(taskAuthorityUtil).validateCanManageTask(task, userId);

        // When & Then
        assertThatThrownBy(() -> taskService.deleteTask(taskId, userId))
                .isInstanceOf(BusinessException.class);

        verify(taskRepository, times(0)).delete(any(Task.class));
    }

    @Test
    @DisplayName("Task 삭제 실패 - Task 없음")
    void deleteTask_Fail_NotFound() {
        // Given
        Long taskId = 999L;
        when(finder.findTaskById(taskId)).thenThrow(new BusinessException(CommonResponse.RESOURCE_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> taskService.deleteTask(taskId, 1L))
                .isInstanceOf(BusinessException.class);

        verify(taskRepository, times(0)).delete(any(Task.class));
    }

    @Test
    @DisplayName("워크스페이스별 Task 목록 조회 성공")
    void getTasksByWorkspace_Success() {
        // Given
        Long workspaceId = 1L;
        Pageable pageable = PageRequest.of(0, 20);
        when(taskAuthorityUtil.canViewAllTasks(workspaceId, 1L)).thenReturn(true);
        when(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(task), pageable, 1));
        when(taskMapper.mapToDto(task)).thenReturn(taskResponseDto);

        // When
        PageResponseDto<TaskResponseDto> result =
                taskService.getTasksByWorkspace(workspaceId, 1L, null, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Task");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("워크스페이스 참여자가 아니면 Task 목록을 볼 수 없다")
    void getTasksByWorkspace_Fail_NotMember() {
        Long workspaceId = 1L;
        doThrow(new AccessDeniedException("워크스페이스 참여자만 접근할 수 있습니다."))
                .when(taskAuthorityUtil).validateWorkspaceMember(workspaceId, 99L);

        assertThatThrownBy(() ->
                taskService.getTasksByWorkspace(workspaceId, 99L, null, PageRequest.of(0, 20)))
                .isInstanceOf(AccessDeniedException.class);

        verify(taskRepository, times(0))
                .findAll(ArgumentMatchers.<Specification<Task>>any(), any(Pageable.class));
    }

    @Test
    @DisplayName("전체 조회 권한이 없으면 참여 조건이 붙어 다른 조회가 나간다")
    void getTasksByWorkspace_Member_NarrowsSpec() {
        Long workspaceId = 1L;
        Pageable pageable = PageRequest.of(0, 20);
        when(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        when(taskAuthorityUtil.canViewAllTasks(workspaceId, 1L)).thenReturn(true);
        taskService.getTasksByWorkspace(workspaceId, 1L, null, pageable);

        when(taskAuthorityUtil.canViewAllTasks(workspaceId, 2L)).thenReturn(false);
        taskService.getTasksByWorkspace(workspaceId, 2L, null, pageable);

        ArgumentCaptor<Specification<Task>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(taskRepository, times(2)).findAll(captor.capture(), eq(pageable));
        assertThat(captor.getAllValues().get(0)).isNotEqualTo(captor.getAllValues().get(1));
    }

    @Test
    @DisplayName("진행률은 상태별로 집계된다")
    void getWorkspaceProgress_Counts() {
        Long workspaceId = 1L;
        when(taskAuthorityUtil.canViewAllTasks(workspaceId, 1L)).thenReturn(true);
        when(todoRepository.count(ArgumentMatchers.<Specification<Todo>>any()))
                .thenReturn(10L, 4L, 3L, 2L);

        TodoProgressDto result = taskService.getWorkspaceProgress(workspaceId, 1L);

        assertThat(result.getTotal()).isEqualTo(10);
        assertThat(result.getInProgress()).isEqualTo(4);
        assertThat(result.getWaitingReview()).isEqualTo(3);
        assertThat(result.getConfirmed()).isEqualTo(2);
    }

    @Test
    @DisplayName("워크스페이스 참여자가 아니면 진행률을 볼 수 없다")
    void getWorkspaceProgress_Fail_NotMember() {
        Long workspaceId = 1L;
        doThrow(new AccessDeniedException("워크스페이스 참여자만 접근할 수 있습니다."))
                .when(taskAuthorityUtil).validateWorkspaceMember(workspaceId, 99L);

        assertThatThrownBy(() -> taskService.getWorkspaceProgress(workspaceId, 99L))
                .isInstanceOf(AccessDeniedException.class);

        verify(todoRepository, times(0)).count(ArgumentMatchers.<Specification<Todo>>any());
    }

    @Test
    @DisplayName("Task에 사용자 추가 성공")
    void assignUserToTask_Success() {
        // Given
        Long taskId = 1L;
        Long targetUserId = 2L;
        Long requesterId = 1L;
        User targetUser = User.builder().id(2L).build();

        when(finder.findTaskById(taskId)).thenReturn(task);
        when(finder.findUserById(targetUserId)).thenReturn(targetUser);
        when(taskUserRepository.existsByTaskAndUser(task, targetUser)).thenReturn(false);
        when(taskUserRepository.save(any(TaskUser.class))).thenReturn(taskUser);

        // When
        taskService.assignUserToTask(taskId, targetUserId, requesterId);

        // Then
        verify(finder, times(1)).findTaskById(taskId);
        verify(taskAuthorityUtil, times(1)).validateWorkspaceMaster(task, requesterId);
        verify(finder, times(1)).findUserById(targetUserId);
        verify(taskUserRepository, times(1)).save(any(TaskUser.class));
    }

    @Test
    @DisplayName("Task에 사용자 추가 실패 - 이미 존재")
    void assignUserToTask_Fail_AlreadyExists() {
        // Given
        Long taskId = 1L;
        Long targetUserId = 2L;
        Long requesterId = 1L;
        User targetUser = User.builder().id(2L).build();

        when(finder.findTaskById(taskId)).thenReturn(task);
        when(finder.findUserById(targetUserId)).thenReturn(targetUser);
        when(taskUserRepository.existsByTaskAndUser(task, targetUser)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> taskService.assignUserToTask(taskId, targetUserId, requesterId))
                .isInstanceOf(BusinessException.class);

        verify(taskUserRepository, times(0)).save(any(TaskUser.class));
    }

    @Test
    @DisplayName("Task에서 사용자 제거 성공")
    void removeUserFromTask_Success() {
        // Given
        Long taskId = 1L;
        Long targetUserId = 2L;
        Long requesterId = 1L;
        User targetUser = User.builder().id(2L).build();
        TaskUser targetTaskUser = TaskUser.builder().build();

        when(finder.findTaskById(taskId)).thenReturn(task);
        when(finder.findUserById(targetUserId)).thenReturn(targetUser);
        when(taskUserRepository.findByTaskAndUser(task, targetUser))
                .thenReturn(Optional.of(targetTaskUser));
        when(taskAuthorityUtil.isTaskManager(task, requesterId)).thenReturn(true);

        // When
        taskService.removeUserFromTask(taskId, targetUserId, requesterId);

        // Then
        verify(finder, times(1)).findTaskById(taskId);
        verify(taskAuthorityUtil, times(1)).validateCanManageTask(task, requesterId);
        verify(taskUserRepository, times(1)).delete(targetTaskUser);
    }

    @Test
    @DisplayName("Task에서 사용자 제거 실패 - 자기 자신 제거 (Manager)")
    void removeUserFromTask_Fail_CannotRemoveSelf() {
        // Given
        Long taskId = 1L;
        Long userId = 1L; // requesterId와 targetUserId가 동일
        when(finder.findTaskById(taskId)).thenReturn(task);
        when(taskAuthorityUtil.isTaskManager(task, userId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> taskService.removeUserFromTask(taskId, userId, userId))
                .isInstanceOf(BusinessException.class);

        verify(taskUserRepository, times(0)).delete(any(TaskUser.class));
    }

    @Test
    @DisplayName("Task Manager 지정 성공")
    void assignTaskManager_Success() {
        // Given
        Long taskId = 1L;
        Long newManagerId = 2L;
        Long requesterId = 1L;
        User newManager = User.builder().id(2L).build();
        TaskUser currentManager = TaskUser.builder().role(TaskRole.MANAGER).build();
        TaskUser newManagerTaskUser = TaskUser.builder()
                .role(TaskRole.NORMAL)
                .user(newManager)
                .build();

        when(finder.findTaskById(taskId)).thenReturn(task);
        when(finder.findUserById(newManagerId)).thenReturn(newManager);
        when(taskUserRepository.findByTaskAndUser(task, newManager))
                .thenReturn(Optional.of(newManagerTaskUser));
        when(taskUserRepository.findAllByTask(task)).thenReturn(Arrays.asList(currentManager));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskUserRepository.save(any(TaskUser.class))).thenReturn(newManagerTaskUser);

        // When
        taskService.assignTaskManager(taskId, newManagerId, requesterId);

        // Then
        verify(finder, times(1)).findTaskById(taskId);
        verify(taskAuthorityUtil, times(1)).validateWorkspaceMaster(task, requesterId);
        verify(finder, times(1)).findUserById(newManagerId);
        verify(taskRepository, times(1)).save(task);
        verify(taskUserRepository, times(2)).save(any(TaskUser.class));
    }

    @Test
    @DisplayName("Task Manager 지정 실패 - 사용자가 Task에 없음")
    void assignTaskManager_Fail_UserNotInTask() {
        // Given
        Long taskId = 1L;
        Long newManagerId = 2L;
        Long requesterId = 1L;
        User newManager = User.builder().id(2L).build();

        when(finder.findTaskById(taskId)).thenReturn(task);
        when(finder.findUserById(newManagerId)).thenReturn(newManager);
        when(taskUserRepository.findByTaskAndUser(task, newManager))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.assignTaskManager(taskId, newManagerId, requesterId))
                .isInstanceOf(BusinessException.class);

        verify(taskRepository, times(0)).save(any(Task.class));
    }

    @Test
    @DisplayName("Task 멤버 목록 조회 성공")
    void getTaskMembers_Success() {
        // Given
        Long taskId = 1L;
        Pageable pageable = PageRequest.of(0, 20);
        TaskUser taskUser1 = TaskUser.builder().build();
        TaskUserResponseDto taskUserResponseDto = TaskUserResponseDto.builder().build();

        when(taskUserRepository.findAllByTask_Id(taskId, pageable))
                .thenReturn(new PageImpl<>(List.of(taskUser1), pageable, 1));
        when(taskUserMapper.mapToDto(taskUser1)).thenReturn(taskUserResponseDto);

        // When
        PageResponseDto<TaskUserResponseDto> result =
                taskService.getTaskMembers(taskId, 1L, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        verify(taskAuthorityUtil, times(1)).requireViewableTask(taskId, 1L);
    }

    @Test
    @DisplayName("볼 수 없는 Task 는 참여자 목록도 막힌다")
    void getTaskMembers_Fail_CannotView() {
        Long taskId = 1L;
        when(taskAuthorityUtil.requireViewableTask(taskId, 99L))
                .thenThrow(new AccessDeniedException("Task 를 볼 권한이 없습니다."));

        assertThatThrownBy(() ->
                taskService.getTaskMembers(taskId, 99L, PageRequest.of(0, 20)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Task 진행률은 상태별로 집계된다")
    void getTaskProgress_Counts() {
        Long taskId = 1L;
        when(taskAuthorityUtil.requireViewableTask(taskId, 1L)).thenReturn(task);
        when(todoRepository.count(ArgumentMatchers.<Specification<Todo>>any()))
                .thenReturn(9L, 5L, 3L, 1L);

        TodoProgressDto result = taskService.getTaskProgress(taskId, 1L);

        assertThat(result.getTotal()).isEqualTo(9);
        assertThat(result.getInProgress()).isEqualTo(5);
        assertThat(result.getWaitingReview()).isEqualTo(3);
        assertThat(result.getConfirmed()).isEqualTo(1);
    }

    @Test
    @DisplayName("이미 참여한 사용자는 다시 추가할 수 없다")
    void assignUserToTask_Fail_AlreadyMember() {
        when(finder.findTaskById(1L)).thenReturn(task);
        when(finder.findUserById(2L)).thenReturn(user);
        when(taskUserRepository.existsByTaskAndUser(task, user)).thenReturn(true);

        assertThatThrownBy(() -> taskService.assignUserToTask(1L, 2L, 1L))
                .isInstanceOf(BusinessException.class);

        verify(taskUserRepository, times(0)).save(any(TaskUser.class));
    }

    @Test
    @DisplayName("Task Manager 는 자기 자신을 뺄 수 없다")
    void removeUserFromTask_Fail_ManagerSelf() {
        when(finder.findTaskById(1L)).thenReturn(task);
        when(taskAuthorityUtil.isTaskManager(task, 1L)).thenReturn(true);

        assertThatThrownBy(() -> taskService.removeUserFromTask(1L, 1L, 1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("검색어가 비어 있으면 조건을 더하지 않는다")
    void getTasksByWorkspace_BlankKeyword() {
        Pageable pageable = PageRequest.of(0, 20);
        when(taskAuthorityUtil.canViewAllTasks(1L, 1L)).thenReturn(true);
        when(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        taskService.getTasksByWorkspace(1L, 1L, "   ", pageable);

        verify(taskRepository, times(1))
                .findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable));
    }

    @Test
    @DisplayName("워크스페이스 진행률도 MEMBER 는 참여한 Task 만 센다")
    void getWorkspaceProgress_MemberScope() {
        when(taskAuthorityUtil.canViewAllTasks(1L, 2L)).thenReturn(false);
        when(todoRepository.count(ArgumentMatchers.<Specification<Todo>>any()))
                .thenReturn(3L, 1L, 1L, 1L);

        TodoProgressDto result = taskService.getWorkspaceProgress(1L, 2L);

        assertThat(result.getTotal()).isEqualTo(3);
    }

    private TaskRequestDto request(Long managerId, List<Long> memberIds) {
        TaskRequestDto dto = new TaskRequestDto();
        dto.setTitle("t");
        dto.setWorkspaceId(1L);
        dto.setManagerId(managerId);
        dto.setMemberIds(memberIds);
        return dto;
    }

    @Test
    @DisplayName("managerId 를 주면 그 사람이 Task Manager 가 되고 알림을 받는다")
    void createTask_WithExplicitManager() {
        Long creatorId = 1L;
        Long managerId = 7L;
        User manager = User.builder().id(managerId).build();
        TaskRequestDto dto = request(managerId, null);

        when(finder.findUserById(creatorId)).thenReturn(user);
        when(finder.findUserById(managerId)).thenReturn(manager);
        when(finder.findWorkspaceById(1L)).thenReturn(workspace);
        when(taskMapper.mapToEntity(any(), any(), any())).thenReturn(task);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.mapToDto(task)).thenReturn(taskResponseDto);

        taskService.createTask(dto, creatorId);

        verify(notificationService, times(1)).createNotification(
                eq(managerId), any(), ArgumentMatchers.anyString(), any());
    }

    @Test
    @DisplayName("멤버 목록에 Task Manager 가 있으면 건너뛴다")
    void createTask_SkipsManagerInMembers() {
        Long creatorId = 1L;
        TaskRequestDto dto = request(null, List.of(creatorId));

        when(finder.findUserById(creatorId)).thenReturn(user);
        when(finder.findWorkspaceById(1L)).thenReturn(workspace);
        when(taskMapper.mapToEntity(any(), any(), any())).thenReturn(task);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.mapToDto(task)).thenReturn(taskResponseDto);

        taskService.createTask(dto, creatorId);

        verify(taskUserRepository, times(1)).save(any(TaskUser.class));
        verify(notificationService, times(0)).createNotification(
                any(), any(), ArgumentMatchers.anyString(), any());
    }

    @Test
    @DisplayName("추가한 멤버는 TaskUser 로 담기고 알림을 받는다")
    void createTask_AddsMembers() {
        Long creatorId = 1L;
        Long memberId = 8L;
        TaskRequestDto dto = request(null, List.of(memberId));

        when(finder.findUserById(creatorId)).thenReturn(user);
        when(finder.findUserById(memberId)).thenReturn(User.builder().id(memberId).build());
        when(finder.findWorkspaceById(1L)).thenReturn(workspace);
        when(taskMapper.mapToEntity(any(), any(), any())).thenReturn(task);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.mapToDto(task)).thenReturn(taskResponseDto);

        taskService.createTask(dto, creatorId);

        verify(taskUserRepository, times(2)).save(any(TaskUser.class));
        verify(notificationService, times(1)).createNotification(
                eq(memberId), any(), ArgumentMatchers.anyString(), any());
    }

    @Test
    @DisplayName("멤버 목록이 비어 있으면 추가로 담지 않는다")
    void createTask_EmptyMembers() {
        Long creatorId = 1L;
        TaskRequestDto dto = request(null, List.of());

        when(finder.findUserById(creatorId)).thenReturn(user);
        when(finder.findWorkspaceById(1L)).thenReturn(workspace);
        when(taskMapper.mapToEntity(any(), any(), any())).thenReturn(task);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.mapToDto(task)).thenReturn(taskResponseDto);

        taskService.createTask(dto, creatorId);

        verify(taskUserRepository, times(1)).save(any(TaskUser.class));
    }

    @Test
    @DisplayName("본인을 추가하면 알림을 보내지 않는다")
    void assignUserToTask_SelfNoNotification() {
        when(finder.findTaskById(1L)).thenReturn(task);
        when(finder.findUserById(1L)).thenReturn(user);
        when(taskUserRepository.existsByTaskAndUser(task, user)).thenReturn(false);

        taskService.assignUserToTask(1L, 1L, 1L);

        verify(notificationService, times(0)).createNotification(
                any(), any(), ArgumentMatchers.anyString(), any());
    }
}
