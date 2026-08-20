package com.muscat.Collabus.common.util;

import com.muscat.Collabus.enums.response.CommonResponse;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.Todo.repository.TodoRepository;
import com.muscat.Collabus.Todo.entity.Todo;
import com.muscat.Collabus.Task.repository.TaskRepository;
import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Workspace.entity.Workspace;
import com.muscat.Collabus.Task.repository.TaskUserRepository;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUser;
import com.muscat.Collabus.WorkspaceUser.repository.WorkspaceUserRepository;
import com.muscat.Collabus.enums.role.WorkspaceRole;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskAuthorityUtil {

  private final TodoRepository todoRepository;
  private final TaskRepository taskRepository;
  private final WorkspaceUserRepository workspaceUserRepository;
  private final TaskUserRepository taskUserRepository;

  // Task 기반 Workspace Master 권한 검증
  public void validateWorkspaceMaster(Task task, Long userId) {
    if (!isWorkspaceMaster(task, userId)) {
      throw new BusinessException(CommonResponse.WORKSPACE_MASTER_REQUIRED);
    }
  }

  // Workspace 기반 Workspace Master 권한 검증
  public void validateWorkspaceMaster(Workspace workspace, Long userId) {
    if (!isWorkspaceMaster(workspace, userId)) {
      throw new BusinessException(CommonResponse.WORKSPACE_MASTER_REQUIRED);
    }
  }

  // Task Manager 권한 검증
  public void validateTaskManager(Task task, Long userId) {
    if (!isTaskManager(task, userId)) {
      throw new BusinessException(CommonResponse.TASK_MANAGER_REQUIRED);
    }
  }

  // Task 관리 권한 (WM or TM) 검증
  public void validateCanManageTask(Task task, Long userId) {
    if (!canManageTask(task, userId)) {
      throw new BusinessException(CommonResponse.TASK_MANAGE_DENIED);
    }
  }

  // Workspace 역할 조회 (WorkspaceUser.role 기준 — founder 필드 아님)
  private WorkspaceRole getWorkspaceRole(Long workspaceId, Long userId) {
    return workspaceUserRepository.findById_WorkspaceIdAndId_UserId(workspaceId, userId)
        .map(WorkspaceUser::getRole)
        .orElse(null);
  }

  // Task 기준 Workspace Master 여부
  public boolean isWorkspaceMaster(Task task, Long userId) {
    return getWorkspaceRole(task.getWorkspace().getId(), userId) == WorkspaceRole.MASTER;
  }

  // Workspace 기준 Workspace Master 여부
  public boolean isWorkspaceMaster(Workspace workspace, Long userId) {
    // 엔티티를 로드하지 않고 존재 여부만 본다
    return workspaceUserRepository.existsById_WorkspaceIdAndId_UserIdAndRole(
        workspace.getId(), userId, WorkspaceRole.MASTER);
  }

  // Task Manager 여부
  public boolean isTaskManager(Task task, Long userId) {
    return task.getTaskManager().getId().equals(userId);
  }

  // Task 관리 권한 여부 (WM or TM)
  public boolean canManageTask(Task task, Long userId) {
    return isWorkspaceMaster(task, userId) || isTaskManager(task, userId);
  }

  // Task 생성 권한 검증 (Workspace Master 또는 Manager)
  public void validateCanCreateTask(Workspace workspace, Long userId) {
    if (!canCreateTask(workspace, userId)) {
      throw new BusinessException(CommonResponse.TASK_CREATE_DENIED);
    }
  }

  // 워크스페이스 MASTER·MANAGER 이거나 Task 참여자인지
  public boolean canViewTask(Task task, Long userId) {
    return canViewAllTasks(task.getWorkspace().getId(), userId)
        || taskUserRepository.existsById_TaskIdAndId_UserId(task.getId(), userId);
  }

  // 볼 수 있는 Task 를 돌려준다. 없거나 볼 수 없으면 똑같은 예외를 낸다
  public Task requireViewableTask(Long taskId, Long userId) {
    return taskRepository.findById(taskId)
        .filter(task -> canViewTask(task, userId))
        .orElseThrow(() -> new BusinessException(CommonResponse.TASK_VIEW_DENIED));
  }

  // 볼 수 있는 Todo 를 돌려준다. 없거나 볼 수 없으면 똑같은 예외를 낸다
  public Todo requireViewableTodo(Long todoId, Long userId) {
    return todoRepository.findById(todoId)
        .filter(todo -> canViewTask(todo.getTask(), userId))
        .orElseThrow(() -> new BusinessException(CommonResponse.TODO_VIEW_DENIED));
  }

  // 볼 수 없으면 예외
  public void validateCanViewTask(Task task, Long userId) {
    if (!canViewTask(task, userId)) {
      throw new BusinessException(CommonResponse.TASK_VIEW_DENIED);
    }
  }

  // 워크스페이스 MASTER 또는 MANAGER 인지
  public boolean canViewAllTasks(Long workspaceId, Long userId) {
    return workspaceUserRepository.existsById_WorkspaceIdAndId_UserIdAndRoleIn(
        workspaceId, userId, List.of(WorkspaceRole.MASTER, WorkspaceRole.MANAGER));
  }

  // 워크스페이스 MASTER 가 아니면 예외
  public void validateWorkspaceMaster(Long workspaceId, Long userId) {
    if (!workspaceUserRepository.existsById_WorkspaceIdAndId_UserIdAndRole(
        workspaceId, userId, WorkspaceRole.MASTER)) {
      throw new BusinessException(CommonResponse.WORKSPACE_MASTER_REQUIRED);
    }
  }

  // 워크스페이스 멤버가 아니면 예외
  public void validateWorkspaceMember(Long workspaceId, Long userId) {
    if (!workspaceUserRepository.existsById_WorkspaceIdAndId_UserId(workspaceId, userId)) {
      throw new BusinessException(CommonResponse.WORKSPACE_PARTICIPANT_REQUIRED);
    }
  }

  // Task 생성 가능 여부 (Workspace MASTER 또는 MANAGER)
  public boolean canCreateTask(Workspace workspace, Long userId) {
    WorkspaceRole role = getWorkspaceRole(workspace.getId(), userId);
    return role == WorkspaceRole.MASTER || role == WorkspaceRole.MANAGER;
  }
}
