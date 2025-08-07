package com.muscat.Collabus.WorkspaceUser.mapper;

import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.Workspace.entity.Workspace;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceInvite;
import com.muscat.Collabus.WorkspaceUser.model.InviteResponseDto;
import com.muscat.Collabus.WorkspaceUser.model.InviteRequestDto;
import com.muscat.Collabus.enums.status.InviteStatus;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceInviteMapper {

  public WorkspaceInvite mapToEntity(Workspace workspace, User inviter, User invitee, InviteRequestDto dto) {
    return WorkspaceInvite.builder()
        .workspace(workspace)
        .inviter(inviter)
        .invitee(invitee)
        .role(dto.getRole())
        .status(InviteStatus.PENDING)
        .build();
  }

  public InviteResponseDto mapToDto(WorkspaceInvite invite) {
    return InviteResponseDto.builder()
        .inviteId(invite.getId())
        .workspaceId(invite.getWorkspace().getId())
        .workspaceName(invite.getWorkspace().getWorkspaceName())
        .inviterDisplayName(invite.getInviter().getDisplayName())
        .role(invite.getRole())
        .status(invite.getStatus())
        .build();
  }
}
