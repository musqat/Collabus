package com.muscat.Collabus.Task.service.impl;

import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Task.entity.TaskUser;
import com.muscat.Collabus.Task.entity.TaskUserPk;
import com.muscat.Collabus.Task.mapper.TaskMapper;
import com.muscat.Collabus.Task.mapper.TaskUserMapper;
import com.muscat.Collabus.Task.model.TaskRequestDto;
import com.muscat.Collabus.Task.model.TaskResponseDto;
import com.muscat.Collabus.Task.model.TaskUpdateRequestDto;
import com.muscat.Collabus.Task.model.TaskUserResponseDto;
import com.muscat.Collabus.Task.repository.TaskRepository;
import com.muscat.Collabus.Task.repository.TaskUserRepository;
import com.muscat.Collabus.Task.service.TaskService;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.Workspace.entity.Workspace;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.common.exception.ResourceNotFoundException;
import com.muscat.Collabus.common.util.EntityFinderUtil;
import com.muscat.Collabus.common.util.TaskAuthorityUtil;
import com.muscat.Collabus.enums.response.CommonResponse;
import com.muscat.Collabus.enums.response.TaskResponse;
import com.muscat.Collabus.enums.role.TaskRole;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

  private final TaskRepository taskRepository;
  private final TaskUserRepository taskUserRepository;
  private final TaskMapper taskMapper;
  private final TaskUserMapper taskUserMapper;
  private final TaskAuthorityUtil taskAuthorityUtil;
  private final EntityFinderUtil finder;

  @Transactional
  @Override
  public TaskResponseDto createTask(TaskRequestDto dto, Long userId) {
    User creator = finder.findUserById(userId);
    Workspace workspace = finder.findWorkspaceById(dto.getWorkspaceId());
    taskAuthorityUtil.validateWorkspaceMaster(workspace, userId);

    Task task = taskRepository.save(taskMapper.mapToEntity(dto, workspace, creator));
    task.setTaskManager(creator);

    taskUserRepository.save(new TaskUser(
        new TaskUserPk(task.getId(), creator.getId()), task, creator, TaskRole.MANAGER));

    return taskMapper.mapToDto(task);
  }

  @Override
  public TaskResponseDto getTask(Long taskId) {
    return taskMapper.mapToDto(finder.findTaskById(taskId));
  }

  @Transactional
  @Override
  public TaskResponseDto updateTask(Long taskId, TaskUpdateRequestDto dto, Long userId) {
    Task task = finder.findTaskById(taskId);
    taskAuthorityUtil.validateCanManageTask(task, userId);

    task.setTitle(dto.getTitle());
    task.setDescription(dto.getDescription());
    task.setDueDate(dto.getDueDate());
    return taskMapper.mapToDto(task);
  }

  @Transactional
  @Override
  public void deleteTask(Long taskId) {
    Task task = finder.findTaskById(taskId);

    taskUserRepository.deleteAllByTask(task);

    taskRepository.delete(finder.findTaskById(taskId));
  }

  @Override
  public List<TaskResponseDto> getTasksByWorkspace(Long workspaceId) {
    return taskRepository.findAllByWorkspace_Id(workspaceId)
        .stream().map(taskMapper::mapToDto).toList();
  }

  @Transactional
  @Override
  public void assignUserToTask(Long taskId, Long targetUserId, Long requesterId) {
    Task task = finder.findTaskById(taskId);
    taskAuthorityUtil.validateWorkspaceMaster(task, requesterId);

    User targetUser = finder.findUserById(targetUserId);
    if (taskUserRepository.existsByTaskAndUser(task, targetUser)) {
      throw new BusinessException(TaskResponse.TASK_USER_ALREADY_EXISTS);
    }

    taskUserRepository.save(new TaskUser(
        new TaskUserPk(taskId, targetUserId), task, targetUser, TaskRole.NORMAL));
  }

  @Transactional
  @Override
  public void removeUserFromTask(Long taskId, Long targetUserId, Long requesterId) {
    Task task = finder.findTaskById(taskId);
    taskAuthorityUtil.validateCanManageTask(task, requesterId);

    if (taskAuthorityUtil.isTaskManager(task, requesterId) && requesterId.equals(targetUserId)) {
      throw new BusinessException(CommonResponse.CANNOT_REMOVE_SELF);
    }

    TaskUser taskUser = taskUserRepository.findByTaskAndUser(task,
            finder.findUserById(targetUserId))
        .orElseThrow(
            () -> new ResourceNotFoundException(TaskResponse.TASK_USER_NOT_FOUND));
    taskUserRepository.delete(taskUser);
  }

  @Transactional
  @Override
  public void assignTaskManager(Long taskId, Long newManagerId, Long requesterId) {
    Task task = finder.findTaskById(taskId);
    taskAuthorityUtil.validateWorkspaceMaster(task, requesterId);

    User newManager = finder.findUserById(newManagerId);
    TaskUser newManagerTaskUser = taskUserRepository.findByTaskAndUser(task, newManager)
        .orElseThrow(
            () -> new ResourceNotFoundException(TaskResponse.TASK_USER_NOT_FOUND));

    taskUserRepository.findAllByTask(task).stream()
        .filter(tu -> tu.getRole() == TaskRole.MANAGER)
        .findFirst()
        .ifPresent(current -> {
          current.setRole(TaskRole.NORMAL);
          taskUserRepository.save(current);
        });

    newManagerTaskUser.setRole(TaskRole.MANAGER);
    task.setTaskManager(newManager);
    taskRepository.save(task);
    taskUserRepository.save(newManagerTaskUser);
  }

  @Override
  public List<TaskUserResponseDto> getTaskMembers(Long taskId) {
    return taskUserRepository.findAllByTask(finder.findTaskById(taskId)).stream()
        .map(taskUserMapper::mapToDto).toList();
  }
}
