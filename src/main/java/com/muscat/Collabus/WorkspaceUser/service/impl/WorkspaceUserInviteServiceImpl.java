package com.muscat.Collabus.WorkspaceUser.service.impl;

import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.Workspace.entity.Workspace;
import com.muscat.Collabus.Workspace.repository.WorkspaceRepository;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceInvite;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUser;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUserPk;
import com.muscat.Collabus.WorkspaceUser.mapper.WorkspaceInviteMapper;
import com.muscat.Collabus.WorkspaceUser.model.InviteRequestDto;
import com.muscat.Collabus.WorkspaceUser.model.InviteResponseDto;
import com.muscat.Collabus.WorkspaceUser.repository.WorkspaceInviteRepository;
import com.muscat.Collabus.WorkspaceUser.repository.WorkspaceUserRepository;
import com.muscat.Collabus.WorkspaceUser.service.WorkspaceUserInviteService;
import com.muscat.Collabus.common.exception.ResourceAlreadyExistsException;
import com.muscat.Collabus.common.exception.ResourceNotFoundException;
import com.muscat.Collabus.enums.response.CommonResponse;
import com.muscat.Collabus.enums.response.InviteResponse;
import com.muscat.Collabus.enums.response.WorkspaceUserResponse;
import com.muscat.Collabus.enums.status.InviteStatus;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkspaceUserInviteServiceImpl implements WorkspaceUserInviteService {

  private final WorkspaceRepository workspaceRepository;
  private final UserRepository userRepository;
  private final WorkspaceInviteRepository inviteRepository;
  private final WorkspaceUserRepository workspaceUserRepository;
  private final WorkspaceInviteMapper inviteMapper;


  @Override
  public void inviteUserToWorkspace(Long inviterId, Long workspaceId, InviteRequestDto dto) {
    if (inviterId.equals(dto.getUserId())) {
      throw new IllegalArgumentException(InviteResponse.INVITE_SELF.getMessage());
    }

    if (workspaceUserRepository.existsById(new WorkspaceUserPk(workspaceId, dto.getUserId()))) {
      throw new ResourceAlreadyExistsException(WorkspaceUserResponse.USER_ALREADY_MEMBER);
    }

    if (inviteRepository.existsByWorkspaceIdAndInviteeIdAndStatus(workspaceId, dto.getUserId(),
        InviteStatus.PENDING)) {
      throw new ResourceAlreadyExistsException(InviteResponse.INVITE_ALREADY_PENDING);
    }

    Workspace workspace = workspaceRepository.findById(workspaceId)
        .orElseThrow(
            () -> new ResourceNotFoundException(CommonResponse.WORKSPACE_NOT_FOUND));
    User inviter = userRepository.findById(inviterId)
        .orElseThrow(() -> new ResourceNotFoundException(CommonResponse.USER_NOT_FOUND));
    User invitee = userRepository.findById(dto.getUserId())
        .orElseThrow(() -> new ResourceNotFoundException(CommonResponse.USER_NOT_FOUND));

    WorkspaceInvite invite = inviteMapper.mapToEntity(workspace, inviter, invitee, dto);
    inviteRepository.save(invite);
  }

  @Override
  public List<InviteResponseDto> getMyInvites(Long inviteeId) {
    return inviteRepository.findAllByInviteeIdAndStatus(inviteeId, InviteStatus.PENDING).stream()
        .map(inviteMapper::mapToDto)
        .collect(Collectors.toList());
  }

  @Override
  public void acceptInvite(Long inviteId, Long inviteeId) {
    WorkspaceInvite invite = inviteRepository.findByIdAndInviteeId(inviteId, inviteeId)
        .orElseThrow(() -> new ResourceNotFoundException(InviteResponse.INVITE_NOT_FOUND));

    if (invite.getStatus() != InviteStatus.PENDING) {
      throw new IllegalStateException(InviteResponse.INVITE_ALREADY_PROCESSED.getMessage());
    }

    invite.setStatus(InviteStatus.ACCEPTED);

    WorkspaceUser workspaceUser = WorkspaceUser.builder()
        .id(new WorkspaceUserPk(invite.getWorkspace().getId(), inviteeId))
        .user(invite.getInvitee())
        .workspace(invite.getWorkspace())
        .role(invite.getRole())
        .build();

    workspaceUserRepository.save(workspaceUser);
  }

  @Override
  public void rejectInvite(Long inviteId, Long inviteeId) {
    WorkspaceInvite invite = inviteRepository.findByIdAndInviteeId(inviteId, inviteeId)
        .orElseThrow(() -> new ResourceNotFoundException(InviteResponse.INVITE_NOT_FOUND));

    invite.setStatus(InviteStatus.REJECTED);
    inviteRepository.save(invite);
  }
}