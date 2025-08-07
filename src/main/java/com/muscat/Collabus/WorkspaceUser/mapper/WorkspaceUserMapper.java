package com.muscat.Collabus.WorkspaceUser.mapper;

import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.Workspace.entity.Workspace;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUser;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUserPk;
import com.muscat.Collabus.WorkspaceUser.model.WorkspaceUserRequestDto;
import com.muscat.Collabus.WorkspaceUser.model.WorkspaceUserResponseDto;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceUserMapper {

  public WorkspaceUser mapToEntity(Long userId, Long workspaceId, WorkspaceUserRequestDto dto) {
    return WorkspaceUser.builder()
        .id(new WorkspaceUserPk(workspaceId, userId))
        .user(User.builder().id(userId).build())
        .workspace(Workspace.builder().id(workspaceId).build())
        .role(dto.getRole())
        .build();
  }

  public WorkspaceUserResponseDto mapToDto(WorkspaceUser workspaceUser) {
    return WorkspaceUserResponseDto.builder()
        .userId(workspaceUser.getUser().getId())
        .workspaceId(workspaceUser.getWorkspace().getId())
        .displayName(workspaceUser.getUser().getDisplayName())
        .role(workspaceUser.getRole())
        .build();
  }
}
