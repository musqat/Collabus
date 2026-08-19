package com.muscat.Collabus.common.util;

import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Task.repository.TaskRepository;
import com.muscat.Collabus.Task.repository.TaskUserRepository;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.WorkspaceUser.repository.WorkspaceUserRepository;
import com.muscat.Collabus.enums.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParticipantUtil {

  private final WorkspaceUserRepository workspaceUserRepository;
  private final TaskUserRepository taskUserRepository;
  private final TaskRepository taskRepository;
  private final UserRepository userRepository;

  // Validate workspace participation
  public void validateWorkspaceParticipant(Long workspaceId, Long userId) {
    if (!workspaceUserRepository.existsById_WorkspaceIdAndId_UserId(workspaceId, userId)) {
      throw new BusinessException(CommonResponse.WORKSPACE_PARTICIPANT_REQUIRED);
    }
  }

  // Validate task participation
  public void validateTaskParticipant(Long taskId, Long userId) {
    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new BusinessException(CommonResponse.TASK_NOT_FOUND));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(CommonResponse.USER_NOT_FOUND));

    if (!taskUserRepository.existsByTaskAndUser(task, user)) {
      throw new BusinessException(CommonResponse.TASK_PARTICIPANT_REQUIRED);
    }
  }
}

