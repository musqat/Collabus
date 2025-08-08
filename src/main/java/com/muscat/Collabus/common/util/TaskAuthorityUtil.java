package com.muscat.Collabus.common.util;

import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Workspace.entity.Workspace;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class TaskAuthorityUtil {

  /**
   * Task 기반 Workspace Master 권한 검증
   */
  public void validateWorkspaceMaster(Task task, Long userId) {
    if (!isWorkspaceMaster(task, userId)) {
      throw new AccessDeniedException("Workspace Master 권한이 필요합니다.");
    }
  }

  /**
   * Workspace 기반 Workspace Master 권한 검증
   */
  public void validateWorkspaceMaster(Workspace workspace, Long userId) {
    if (!isWorkspaceMaster(workspace, userId)) {
      throw new AccessDeniedException("Workspace Master 권한이 필요합니다.");
    }
  }

  /**
   * Task Manager 권한 검증
   */
  public void validateTaskManager(Task task, Long userId) {
    if (!isTaskManager(task, userId)) {
      throw new AccessDeniedException("Task Manager 권한이 필요합니다.");
    }
  }

  /**
   * Task 관리 권한 (WM or TM) 검증
   */
  public void validateCanManageTask(Task task, Long userId) {
    if (!canManageTask(task, userId)) {
      throw new AccessDeniedException("Task 관리 권한이 없습니다.");
    }
  }

  /**
   * Task 기준 Workspace Master 여부
   */
  public boolean isWorkspaceMaster(Task task, Long userId) {
    return task.getWorkspace().getFounder().getId().equals(userId);
  }

  /**
   * Workspace 기준 Workspace Master 여부
   */
  public boolean isWorkspaceMaster(Workspace workspace, Long userId) {
    return workspace.getFounder().getId().equals(userId);
  }

  /**
   * Task Manager 여부
   */
  public boolean isTaskManager(Task task, Long userId) {
    return task.getTaskManager().getId().equals(userId);
  }

  /**
   * Task 관리 권한 여부 (WM or TM)
   */
  public boolean canManageTask(Task task, Long userId) {
    return isWorkspaceMaster(task, userId) || isTaskManager(task, userId);
  }
}
