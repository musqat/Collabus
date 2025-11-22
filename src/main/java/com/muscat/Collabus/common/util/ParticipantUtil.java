package com.muscat.Collabus.common.util;

import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Task.repository.TaskRepository;
import com.muscat.Collabus.Task.repository.TaskUserRepository;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.WorkspaceUser.repository.WorkspaceUserRepository;
import com.muscat.Collabus.common.exception.ResourceNotFoundException;
import com.muscat.Collabus.enums.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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
      throw new AccessDeniedException("사용자가 워크스페이스 참여자가 아닙니다.");
    }
  }

  // Validate task participation
  public void validateTaskParticipant(Long taskId, Long userId) {
    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new ResourceNotFoundException(CommonResponse.TASK_NOT_FOUND));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException(CommonResponse.USER_NOT_FOUND));

    if (!taskUserRepository.existsByTaskAndUser(task, user)) {
      throw new AccessDeniedException("사용자가 태스크 참여자가 아닙니다.");
    }
  }
}

