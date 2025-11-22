package com.muscat.Collabus.Workspace.service.impl;

import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Task.repository.TaskRepository;
import com.muscat.Collabus.Task.repository.TaskUserRepository;
import com.muscat.Collabus.Todo.entity.Todo;
import com.muscat.Collabus.Todo.entity.TodoWork;
import com.muscat.Collabus.Todo.repository.TodoCommentRepository;
import com.muscat.Collabus.Todo.repository.TodoFileRepository;
import com.muscat.Collabus.Todo.repository.TodoRepository;
import com.muscat.Collabus.Todo.repository.TodoWorkRepository;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.Workspace.entity.Workspace;
import com.muscat.Collabus.Workspace.mapper.WorkspaceMapper;
import com.muscat.Collabus.Workspace.model.WorkspaceRequestDto;
import com.muscat.Collabus.Workspace.model.WorkspaceResponseDto;
import com.muscat.Collabus.Workspace.repository.WorkspaceRepository;
import com.muscat.Collabus.Workspace.service.WorkspaceService;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUser;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUserPk;
import com.muscat.Collabus.WorkspaceUser.repository.WorkspaceUserRepository;
import com.muscat.Collabus.common.util.EntityFinderUtil;
import com.muscat.Collabus.common.util.TaskAuthorityUtil;
import com.muscat.Collabus.enums.role.WorkspaceRole;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

  private final WorkspaceRepository workspaceRepository;
  private final WorkspaceMapper workspaceMapper;
  private final WorkspaceUserRepository workspaceUserRepository;
  private final EntityFinderUtil entityFinderUtil;
  private final TaskAuthorityUtil taskAuthorityUtil;
  private final TaskRepository taskRepository;
  private final TaskUserRepository taskUserRepository;
  private final TodoRepository todoRepository;
  private final TodoWorkRepository todoWorkRepository;
  private final TodoCommentRepository todoCommentRepository;
  private final TodoFileRepository todoFileRepository;

  @Override
  @Transactional
  public WorkspaceResponseDto createWorkspace(WorkspaceRequestDto dto, Long founderId) {
    User founder = entityFinderUtil.findUserById(founderId);

    Workspace workspace = Workspace.builder()
        .workspaceName(dto.getWorkspaceName())
        .description(dto.getDescription())
        .founder(founder)
        .build();
    workspaceRepository.save(workspace);

    WorkspaceUser workspaceUser = WorkspaceUser.builder()
        .id(new WorkspaceUserPk(founder.getId(), workspace.getId()))
        .workspace(workspace)
        .user(founder)
        .role(WorkspaceRole.MASTER)
        .build();
    workspaceUserRepository.save(workspaceUser);

    return workspaceMapper.mapToDto(workspace);
  }

  @Override
  public WorkspaceResponseDto getWorkspaceById(Long workspaceId) {
    Workspace workspace = entityFinderUtil.findWorkspaceById(workspaceId);
    return workspaceMapper.mapToDto(workspace);
  }

  @Override
  public List<WorkspaceResponseDto> getMyWorkspaces(Long userId) {
    return workspaceRepository.findAllByFounderId(userId).stream()
        .map(workspaceMapper::mapToDto)
        .collect(Collectors.toList());
  }

  @Override
  public List<WorkspaceResponseDto> getJoinedWorkspaces(Long userId) {
    return workspaceUserRepository.findAllById_UserId(userId).stream()
        .map(WorkspaceUser::getWorkspace)
        .map(workspaceMapper::mapToDto)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public WorkspaceResponseDto updateWorkspace(Long id, WorkspaceRequestDto dto, Long userId) {
    Workspace workspace = entityFinderUtil.findWorkspaceById(id);
    taskAuthorityUtil.validateWorkspaceMaster(workspace, userId);

    workspaceMapper.updateEntity(workspace, dto);
    Workspace updated = workspaceRepository.save(workspace);
    return workspaceMapper.mapToDto(updated);
  }

  @Override
  @Transactional
  public void deleteWorkspace(Long workspaceId, Long userId) {
    taskAuthorityUtil.validateWorkspaceMaster(
        entityFinderUtil.findWorkspaceById(workspaceId), userId);

    List<Task> tasks = taskRepository.findAllByWorkspace_Id(workspaceId);
    for (Task task : tasks) {
      // todo삭제
      List<Todo> todos = todoRepository.findAllByTaskId(task.getId());
      for (Todo todo : todos) {
        //  TodoWork 랑 파일 삭제
        List<TodoWork> todoWorks = todoWorkRepository.findAllByTodoId(todo.getId());
        for (TodoWork work : todoWorks) {
          todoFileRepository.deleteAllByWorkId(work.getId());
        }
        todoWorkRepository.deleteAllByTodoId(todo.getId());

        //  TodoComments 삭제
        todoCommentRepository.deleteAllByTodoId(todo.getId());
      }
      todoRepository.deleteAll(todos);

      //  TaskUsers 삭제
      taskUserRepository.deleteAllByTask(task);
    }
    taskRepository.deleteAll(tasks);

    //  WorkspaceUsers 삭제
    workspaceUserRepository.deleteAllById_WorkspaceId(workspaceId);

    //  Workspace 삭제
    workspaceRepository.deleteById(workspaceId);
  }
}
