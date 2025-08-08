package com.muscat.Collabus.Task.mapper;

import com.muscat.Collabus.Task.entity.TaskUser;
import com.muscat.Collabus.Task.model.TaskUserResponseDto;
import org.springframework.stereotype.Component;

@Component
public class TaskUserMapper {

  public TaskUserResponseDto mapToDto(TaskUser taskUser) {
    return TaskUserResponseDto.builder()
        .userId(taskUser.getUser().getId())
        .displayName(taskUser.getUser().getDisplayName())
        .role(taskUser.getRole())
        .build();
  }
}
