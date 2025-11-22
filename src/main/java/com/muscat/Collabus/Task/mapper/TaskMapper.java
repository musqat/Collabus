package com.muscat.Collabus.Task.mapper;

import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Task.model.TaskRequestDto;
import com.muscat.Collabus.Task.model.TaskResponseDto;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.Workspace.entity.Workspace;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

  public Task mapToEntity(TaskRequestDto request, Workspace workspace, User manager) {
    return Task.builder()
        .title(request.getTitle())
        .description(request.getDescription())
        .dueDate(request.getDueDate())
        .workspace(workspace)
        .taskManager(manager)
        .build();
  }

  public TaskResponseDto mapToDto(Task task) {
    return TaskResponseDto.builder()
        .id(task.getId())
        .title(task.getTitle())
        .description(task.getDescription())
        .dueDate(task.getDueDate())
        .managerDisplayName(task.getTaskManager() != null ? task.getTaskManager().getDisplayName() : "미정")
        .workspaceId(task.getWorkspace().getId())
        .build();
  }
}
