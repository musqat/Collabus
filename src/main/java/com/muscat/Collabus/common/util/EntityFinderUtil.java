package com.muscat.Collabus.common.util;

import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Task.repository.TaskRepository;
import com.muscat.Collabus.Todo.entity.Todo;
import com.muscat.Collabus.Todo.repository.TodoRepository;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.Workspace.entity.Workspace;
import com.muscat.Collabus.Workspace.repository.WorkspaceRepository;
import com.muscat.Collabus.enums.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EntityFinderUtil {

  private final UserRepository userRepository;
  private final WorkspaceRepository workspaceRepository;
  private final TaskRepository taskRepository;
  private final TodoRepository todoRepository;

  public User findUserById(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(CommonResponse.USER_NOT_FOUND));
  }

  public Workspace findWorkspaceById(Long workspaceId) {
    return workspaceRepository.findById(workspaceId)
        .orElseThrow(() -> new BusinessException(CommonResponse.WORKSPACE_NOT_FOUND));
  }

  public Task findTaskById(Long taskId) {
    return taskRepository.findById(taskId)
        .orElseThrow(() -> new BusinessException(CommonResponse.TASK_NOT_FOUND));
  }
  public Todo findTodoById(Long todoId) {
    return todoRepository.findById(todoId)
        .orElseThrow(() -> new BusinessException(CommonResponse.TODO_NOT_FOUND));
  }

}
