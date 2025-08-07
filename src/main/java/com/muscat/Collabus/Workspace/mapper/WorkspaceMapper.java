package com.muscat.Collabus.Workspace.mapper;

import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.Workspace.entity.Workspace;
import com.muscat.Collabus.Workspace.model.WorkspaceRequestDto;
import com.muscat.Collabus.Workspace.model.WorkspaceResponseDto;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceMapper {

  public Workspace mapToEntity(WorkspaceRequestDto dto, Long founderId) {
    return Workspace.builder()
        .workspaceName(dto.getWorkspaceName())
        .description(dto.getDescription())
        .founder(User.builder().id(founderId).build())
        .build();
  }

  public WorkspaceResponseDto mapToDto(Workspace workspace) {
    return WorkspaceResponseDto.builder()
        .id(workspace.getId())
        .workspaceName(workspace.getWorkspaceName())
        .description(workspace.getDescription())
        .founderDisplayName(workspace.getFounder().getDisplayName())
        .build();
  }

  public void updateEntity(Workspace workspace, WorkspaceRequestDto dto) {
    workspace.setWorkspaceName(dto.getWorkspaceName());
    workspace.setDescription(dto.getDescription());
  }
}
