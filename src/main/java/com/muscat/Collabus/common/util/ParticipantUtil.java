package com.muscat.Collabus.common.util;

import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Task.repository.TaskRepository;
import com.muscat.Collabus.Task.repository.TaskUserRepository;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.Workspace.repository.WorkspaceRepository;
import com.muscat.Collabus.WorkspaceUser.repository.WorkspaceUserRepository;
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

  /**
   * 워크스페이스 참여 여부 검증
   */
  public void validateWorkspaceParticipant(Long workspaceId, Long userId) {
    if (!workspaceUserRepository.existsById_WorkspaceIdAndId_UserId(workspaceId, userId)) {
      throw new AccessDeniedException("워크스페이스에 속해있지 않습니다.");
    }
  }

  /**
   * 태스크 참여 여부 검증
   */
  public void validateTaskParticipant(Long taskId, Long userId) {
    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Task입니다."));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

    if (!taskUserRepository.existsByTaskAndUser(task, user)) {
      throw new AccessDeniedException("해당 Task에 참여한 사용자가 아닙니다.");
    }
  }
}
