package com.muscat.Collabus.Workspace.service.impl;

import org.springframework.data.domain.Pageable;
import com.muscat.Collabus.common.util.SortGuard;
import com.muscat.Collabus.common.dto.PageResponseDto;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.Workspace.entity.Workspace;
import com.muscat.Collabus.Workspace.mapper.WorkspaceMapper;
import com.muscat.Collabus.Workspace.model.WorkspaceRequestDto;
import com.muscat.Collabus.Workspace.model.WorkspaceResponseDto;
import com.muscat.Collabus.Todo.event.FilesDeletedEvent;
import com.muscat.Collabus.Todo.repository.TodoFileRepository;
import com.muscat.Collabus.Workspace.repository.WorkspaceRepository;
import com.muscat.Collabus.Workspace.service.WorkspaceService;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUser;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUserPk;
import com.muscat.Collabus.WorkspaceUser.repository.WorkspaceUserRepository;
import com.muscat.Collabus.common.util.EntityFinderUtil;
import com.muscat.Collabus.common.util.TaskAuthorityUtil;
import com.muscat.Collabus.enums.role.WorkspaceRole;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceServiceImpl implements WorkspaceService {

  private final SortGuard sortGuard;
  private final WorkspaceRepository workspaceRepository;
  private final TodoFileRepository todoFileRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final WorkspaceMapper workspaceMapper;
  private final WorkspaceUserRepository workspaceUserRepository;
  private final EntityFinderUtil entityFinderUtil;
  private final TaskAuthorityUtil taskAuthorityUtil;

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
        .id(new WorkspaceUserPk(workspace.getId(), founder.getId()))
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

  // 내가 만든 것만. 멤버로 참여만 한 워크스페이스는 getJoinedWorkspaces 를 쓴다
  @Override
  public PageResponseDto<WorkspaceResponseDto> getMyWorkspaces(Long userId, Pageable pageable) {
    return PageResponseDto.of(
        workspaceRepository.findAllByFounderId(userId, sortGuard.apply(pageable, Workspace.class)),
        workspaceMapper::mapToDto);
  }

  // 멤버로 참여 중인 전부. 내가 만든 것도 포함된다
  @Override
  public PageResponseDto<WorkspaceResponseDto> getJoinedWorkspaces(Long userId,
      Pageable pageable) {
    return PageResponseDto.of(
        workspaceUserRepository.findAllById_UserId(userId,
            sortGuard.apply(pageable, WorkspaceUser.class)),
        workspaceUser -> workspaceMapper.mapToDto(workspaceUser.getWorkspace()));
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
    Workspace workspace = entityFinderUtil.findWorkspaceById(workspaceId);
    taskAuthorityUtil.validateWorkspaceMaster(workspace, userId);

    // 레코드는 FK 의 ON DELETE CASCADE 가 지우지만 디스크 파일은 남는다
    // 지워질 파일 경로를 미리 모아두고 커밋 이후에 정리한다.
    List<String> fileUrls = todoFileRepository.findAllByWork_Todo_Task_Workspace_Id(workspaceId)
        .stream().map(TodoFileRepository.FileLocation::getFileUrl).toList();

    workspaceRepository.delete(workspace);
    eventPublisher.publishEvent(new FilesDeletedEvent(fileUrls));
  }
}
