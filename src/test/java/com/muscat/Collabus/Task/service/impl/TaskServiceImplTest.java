package com.muscat.Collabus.Task.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Task.entity.TaskUser;
import com.muscat.Collabus.Task.mapper.TaskMapper;
import com.muscat.Collabus.Task.mapper.TaskUserMapper;
import com.muscat.Collabus.Task.model.TaskRequestDto;
import com.muscat.Collabus.Task.model.TaskResponseDto;
import com.muscat.Collabus.Task.model.TaskUpdateRequestDto;
import com.muscat.Collabus.Task.model.TaskUserResponseDto;
import com.muscat.Collabus.Task.repository.TaskRepository;
import com.muscat.Collabus.Task.repository.TaskUserRepository;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.Workspace.entity.Workspace;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.common.exception.ResourceNotFoundException;
import com.muscat.Collabus.common.util.EntityFinderUtil;
import com.muscat.Collabus.common.util.TaskAuthorityUtil;
import com.muscat.Collabus.enums.role.SystemRole;
import com.muscat.Collabus.enums.role.TaskRole;
import java.time.LocalDate;
import java.util.Arrays;
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
@DisplayName("TaskService 단위 테스트")
class TaskServiceImplTest {

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
    verify(taskAuthorityUtil, times(1)).validateWorkspaceMaster(workspace, userId);
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
    doThrow(new BusinessException(null))
        .when(taskAuthorityUtil).validateWorkspaceMaster(workspace, userId);

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
    when(finder.findTaskById(taskId)).thenReturn(task);
    when(taskMapper.mapToDto(task)).thenReturn(taskResponseDto);

    // When
    TaskResponseDto result = taskService.getTask(taskId);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(taskId);
    verify(finder, times(1)).findTaskById(taskId);
  }

  @Test
  @DisplayName("Task 조회 실패 - 존재하지 않음")
  void getTask_Fail_NotFound() {
    // Given
    Long taskId = 999L;
    when(finder.findTaskById(taskId)).thenThrow(new ResourceNotFoundException(null));

    // When & Then
    assertThatThrownBy(() -> taskService.getTask(taskId))
        .isInstanceOf(ResourceNotFoundException.class);
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
    doThrow(new BusinessException(null))
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
    when(finder.findTaskById(taskId)).thenReturn(task);

    // When
    taskService.deleteTask(taskId);

    // Then
    verify(finder, times(2)).findTaskById(taskId);
    verify(taskUserRepository, times(1)).deleteAllByTask(task);
    verify(taskRepository, times(1)).delete(task);
  }

  @Test
  @DisplayName("Task 삭제 실패 - Task 없음")
  void deleteTask_Fail_NotFound() {
    // Given
    Long taskId = 999L;
    when(finder.findTaskById(taskId)).thenThrow(new ResourceNotFoundException(null));

    // When & Then
    assertThatThrownBy(() -> taskService.deleteTask(taskId))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(taskRepository, times(0)).delete(any(Task.class));
  }

  @Test
  @DisplayName("워크스페이스별 Task 목록 조회 성공")
  void getTasksByWorkspace_Success() {
    // Given
    Long workspaceId = 1L;
    List<Task> tasks = Arrays.asList(task);
    when(taskRepository.findAllByWorkspace_Id(workspaceId)).thenReturn(tasks);
    when(taskMapper.mapToDto(task)).thenReturn(taskResponseDto);

    // When
    List<TaskResponseDto> result = taskService.getTasksByWorkspace(workspaceId);

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).isEqualTo("Test Task");
    verify(taskRepository, times(1)).findAllByWorkspace_Id(workspaceId);
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
    TaskUser targetTaskUser = new TaskUser();

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
    TaskUser currentManager = new TaskUser();
    currentManager.setRole(TaskRole.MANAGER);
    TaskUser newManagerTaskUser = new TaskUser();
    newManagerTaskUser.setRole(TaskRole.NORMAL);
    newManagerTaskUser.setUser(newManager);

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
        .isInstanceOf(ResourceNotFoundException.class);

    verify(taskRepository, times(0)).save(any(Task.class));
  }

  @Test
  @DisplayName("Task 멤버 목록 조회 성공")
  void getTaskMembers_Success() {
    // Given
    Long taskId = 1L;
    TaskUser taskUser1 = new TaskUser();
    List<TaskUser> taskUsers = Arrays.asList(taskUser1);
    TaskUserResponseDto taskUserResponseDto = TaskUserResponseDto.builder().build();

    when(finder.findTaskById(taskId)).thenReturn(task);
    when(taskUserRepository.findAllByTask(task)).thenReturn(taskUsers);
    when(taskUserMapper.mapToDto(taskUser1)).thenReturn(taskUserResponseDto);

    // When
    List<TaskUserResponseDto> result = taskService.getTaskMembers(taskId);

    // Then
    assertThat(result).hasSize(1);
    verify(finder, times(1)).findTaskById(taskId);
    verify(taskUserRepository, times(1)).findAllByTask(task);
  }
}
