package com.muscat.Collabus.Workspace.service.impl;

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

    workspaceUserRepository.deleteAllByWorkspaceId(workspaceId);
    workspaceRepository.deleteById(workspaceId);
  }
}
