package com.muscat.Collabus.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Task.repository.TaskRepository;
import com.muscat.Collabus.Task.repository.TaskUserRepository;
import com.muscat.Collabus.Todo.entity.Todo;
import com.muscat.Collabus.Todo.repository.TodoRepository;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.Workspace.entity.Workspace;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUser;
import com.muscat.Collabus.WorkspaceUser.repository.WorkspaceUserRepository;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.enums.role.WorkspaceRole;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("권한 검증")
class TaskAuthorityUtilTest {

  private static final Long WORKSPACE_ID = 10L;
  private static final Long TASK_ID = 20L;
  private static final Long TODO_ID = 30L;
  private static final Long MEMBER_ID = 1L;
  private static final Long OUTSIDER_ID = 99L;

  @Mock
  private WorkspaceUserRepository workspaceUserRepository;

  @Mock
  private TaskUserRepository taskUserRepository;

  @Mock
  private TaskRepository taskRepository;

  @Mock
  private TodoRepository todoRepository;

  @InjectMocks
  private TaskAuthorityUtil taskAuthorityUtil;

  private Workspace workspace;
  private Task task;
  private Todo todo;

  @BeforeEach
  void setUp() {
    workspace = Workspace.builder().id(WORKSPACE_ID).build();
    User manager = User.builder().id(MEMBER_ID).build();
    task = Task.builder().id(TASK_ID).workspace(workspace).taskManager(manager).build();
    todo = Todo.builder().id(TODO_ID).task(task).build();
  }

  private void asRole(Long userId, WorkspaceRole role) {
    when(workspaceUserRepository.findById_WorkspaceIdAndId_UserId(WORKSPACE_ID, userId))
        .thenReturn(Optional.of(WorkspaceUser.builder().role(role).build()));
  }

  private void seesAllTasks(Long userId, boolean allowed) {
    when(workspaceUserRepository.existsById_WorkspaceIdAndId_UserIdAndRoleIn(
        eq(WORKSPACE_ID), eq(userId), any())).thenReturn(allowed);
  }

  @Test
  @DisplayName("MASTER 만 워크스페이스 관리 권한을 가진다")
  void isWorkspaceMaster() {
    asRole(MEMBER_ID, WorkspaceRole.MASTER);
    assertThat(taskAuthorityUtil.isWorkspaceMaster(task, MEMBER_ID)).isTrue();
  }

  @Test
  @DisplayName("MANAGER 는 워크스페이스 MASTER 가 아니다")
  void isWorkspaceMaster_Manager() {
    asRole(MEMBER_ID, WorkspaceRole.MANAGER);
    assertThat(taskAuthorityUtil.isWorkspaceMaster(task, MEMBER_ID)).isFalse();
  }

  @Test
  @DisplayName("참여자가 아니면 워크스페이스 MASTER 가 아니다")
  void isWorkspaceMaster_NotMember() {
    when(workspaceUserRepository.findById_WorkspaceIdAndId_UserId(WORKSPACE_ID, OUTSIDER_ID))
        .thenReturn(Optional.empty());
    assertThat(taskAuthorityUtil.isWorkspaceMaster(task, OUTSIDER_ID)).isFalse();
  }

  @Test
  @DisplayName("MASTER 가 아니면 검증에서 막힌다")
  void validateWorkspaceMaster_Fail() {
    asRole(OUTSIDER_ID, WorkspaceRole.MEMBER);
    assertThatThrownBy(() -> taskAuthorityUtil.validateWorkspaceMaster(task, OUTSIDER_ID))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("Task Manager 는 본인만이다")
  void isTaskManager() {
    assertThat(taskAuthorityUtil.isTaskManager(task, MEMBER_ID)).isTrue();
    assertThat(taskAuthorityUtil.isTaskManager(task, OUTSIDER_ID)).isFalse();
  }

  @Test
  @DisplayName("Task Manager 가 아니면 막힌다")
  void validateTaskManager_Fail() {
    assertThatThrownBy(() -> taskAuthorityUtil.validateTaskManager(task, OUTSIDER_ID))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("Task Manager 면 워크스페이스 역할을 보지 않고 관리할 수 있다")
  void canManageTask_AsTaskManager() {
    assertThat(taskAuthorityUtil.canManageTask(task, MEMBER_ID)).isTrue();
  }

  @Test
  @DisplayName("워크스페이스 MASTER 도 Task 를 관리할 수 있다")
  void canManageTask_AsWorkspaceMaster() {
    asRole(OUTSIDER_ID, WorkspaceRole.MASTER);
    assertThat(taskAuthorityUtil.canManageTask(task, OUTSIDER_ID)).isTrue();
  }

  @Test
  @DisplayName("둘 다 아니면 Task 관리가 막힌다")
  void validateCanManageTask_Fail() {
    asRole(OUTSIDER_ID, WorkspaceRole.MEMBER);
    assertThatThrownBy(() -> taskAuthorityUtil.validateCanManageTask(task, OUTSIDER_ID))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("Task 생성은 MANAGER 도 가능하다")
  void canCreateTask() {
    asRole(MEMBER_ID, WorkspaceRole.MANAGER);
    assertThat(taskAuthorityUtil.canCreateTask(workspace, MEMBER_ID)).isTrue();
  }

  @Test
  @DisplayName("MEMBER 는 Task 를 만들 수 없다")
  void validateCanCreateTask_Fail() {
    asRole(MEMBER_ID, WorkspaceRole.MEMBER);
    assertThatThrownBy(() -> taskAuthorityUtil.validateCanCreateTask(workspace, MEMBER_ID))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("MASTER·MANAGER 는 참여하지 않은 Task 도 볼 수 있다")
  void canViewTask_ManagerSeesAll() {
    seesAllTasks(OUTSIDER_ID, true);
    assertThat(taskAuthorityUtil.canViewTask(task, OUTSIDER_ID)).isTrue();
  }

  @Test
  @DisplayName("MEMBER 는 참여한 Task 만 볼 수 있다")
  void canViewTask_MemberNeedsParticipation() {
    seesAllTasks(MEMBER_ID, false);
    when(taskUserRepository.existsById_TaskIdAndId_UserId(TASK_ID, MEMBER_ID)).thenReturn(true);
    assertThat(taskAuthorityUtil.canViewTask(task, MEMBER_ID)).isTrue();
  }

  @Test
  @DisplayName("참여하지 않은 MEMBER 는 볼 수 없다")
  void canViewTask_Denied() {
    seesAllTasks(OUTSIDER_ID, false);
    when(taskUserRepository.existsById_TaskIdAndId_UserId(TASK_ID, OUTSIDER_ID)).thenReturn(false);
    assertThatThrownBy(() -> taskAuthorityUtil.validateCanViewTask(task, OUTSIDER_ID))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("없는 Task 도 볼 수 없는 것과 같은 예외를 낸다")
  void requireViewableTask_NotFound() {
    when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> taskAuthorityUtil.requireViewableTask(TASK_ID, MEMBER_ID))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("볼 수 있는 Task 는 그대로 돌려준다")
  void requireViewableTask_Success() {
    when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
    seesAllTasks(MEMBER_ID, true);
    assertThat(taskAuthorityUtil.requireViewableTask(TASK_ID, MEMBER_ID)).isEqualTo(task);
  }

  @Test
  @DisplayName("없는 Todo 도 볼 수 없는 것과 같은 예외를 낸다")
  void requireViewableTodo_NotFound() {
    when(todoRepository.findById(TODO_ID)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> taskAuthorityUtil.requireViewableTodo(TODO_ID, MEMBER_ID))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("볼 수 있는 Todo 는 그대로 돌려준다")
  void requireViewableTodo_Success() {
    when(todoRepository.findById(TODO_ID)).thenReturn(Optional.of(todo));
    seesAllTasks(MEMBER_ID, true);
    assertThat(taskAuthorityUtil.requireViewableTodo(TODO_ID, MEMBER_ID)).isEqualTo(todo);
  }

  @Test
  @DisplayName("워크스페이스 참여자가 아니면 막힌다")
  void validateWorkspaceMember_Fail() {
    when(workspaceUserRepository.existsById_WorkspaceIdAndId_UserId(WORKSPACE_ID, OUTSIDER_ID))
        .thenReturn(false);
    assertThatThrownBy(() -> taskAuthorityUtil.validateWorkspaceMember(WORKSPACE_ID, OUTSIDER_ID))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("워크스페이스 참여자면 통과한다")
  void validateWorkspaceMember_Success() {
    when(workspaceUserRepository.existsById_WorkspaceIdAndId_UserId(WORKSPACE_ID, MEMBER_ID))
        .thenReturn(true);
    assertThatCode(() -> taskAuthorityUtil.validateWorkspaceMember(WORKSPACE_ID, MEMBER_ID))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("MASTER 가 아니면 워크스페이스 관리가 막힌다")
  void validateWorkspaceMasterById_Fail() {
    when(workspaceUserRepository.existsById_WorkspaceIdAndId_UserIdAndRole(
        WORKSPACE_ID, OUTSIDER_ID, WorkspaceRole.MASTER)).thenReturn(false);
    assertThatThrownBy(() -> taskAuthorityUtil.validateWorkspaceMaster(WORKSPACE_ID, OUTSIDER_ID))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("MASTER 면 Task 기준 검증을 통과한다")
  void validateWorkspaceMaster_ByTask_Success() {
    asRole(MEMBER_ID, WorkspaceRole.MASTER);
    assertThatCode(() -> taskAuthorityUtil.validateWorkspaceMaster(task, MEMBER_ID))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("MASTER 면 워크스페이스 기준 검증을 통과한다")
  void validateWorkspaceMaster_ByWorkspace_Success() {
    when(workspaceUserRepository.existsById_WorkspaceIdAndId_UserIdAndRole(
        WORKSPACE_ID, MEMBER_ID, WorkspaceRole.MASTER)).thenReturn(true);
    assertThatCode(() -> taskAuthorityUtil.validateWorkspaceMaster(workspace, MEMBER_ID))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("MASTER 가 아니면 워크스페이스 기준 검증에서 막힌다")
  void validateWorkspaceMaster_ByWorkspace_Fail() {
    when(workspaceUserRepository.existsById_WorkspaceIdAndId_UserIdAndRole(
        WORKSPACE_ID, OUTSIDER_ID, WorkspaceRole.MASTER)).thenReturn(false);
    assertThatThrownBy(() -> taskAuthorityUtil.validateWorkspaceMaster(workspace, OUTSIDER_ID))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("Task Manager 면 검증을 통과한다")
  void validateTaskManager_Success() {
    assertThatCode(() -> taskAuthorityUtil.validateTaskManager(task, MEMBER_ID))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("Task Manager 면 관리 검증을 통과한다")
  void validateCanManageTask_Success() {
    assertThatCode(() -> taskAuthorityUtil.validateCanManageTask(task, MEMBER_ID))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("MANAGER 면 Task 생성 검증을 통과한다")
  void validateCanCreateTask_Success() {
    asRole(MEMBER_ID, WorkspaceRole.MANAGER);
    assertThatCode(() -> taskAuthorityUtil.validateCanCreateTask(workspace, MEMBER_ID))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("볼 수 있으면 조회 검증을 통과한다")
  void validateCanViewTask_Success() {
    seesAllTasks(MEMBER_ID, true);
    assertThatCode(() -> taskAuthorityUtil.validateCanViewTask(task, MEMBER_ID))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("MASTER 면 워크스페이스 아이디 기준 검증을 통과한다")
  void validateWorkspaceMasterById_Success() {
    when(workspaceUserRepository.existsById_WorkspaceIdAndId_UserIdAndRole(
        WORKSPACE_ID, MEMBER_ID, WorkspaceRole.MASTER)).thenReturn(true);
    assertThatCode(() -> taskAuthorityUtil.validateWorkspaceMaster(WORKSPACE_ID, MEMBER_ID))
        .doesNotThrowAnyException();
  }
}
