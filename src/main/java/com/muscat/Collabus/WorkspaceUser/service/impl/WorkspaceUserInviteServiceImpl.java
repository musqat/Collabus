package com.muscat.Collabus.WorkspaceUser.service.impl;

import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.common.util.TaskAuthorityUtil;
import com.muscat.Collabus.common.util.SortGuard;
import com.muscat.Collabus.Notification.service.NotificationService;
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
import com.muscat.Collabus.common.dto.PageResponseDto;
import com.muscat.Collabus.enums.NotificationType;
import com.muscat.Collabus.enums.response.CommonResponse;
import com.muscat.Collabus.enums.response.InviteResponse;
import com.muscat.Collabus.enums.response.WorkspaceUserResponse;
import com.muscat.Collabus.enums.status.InviteStatus;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceUserInviteServiceImpl implements WorkspaceUserInviteService {

    private final TaskAuthorityUtil taskAuthorityUtil;

    private final SortGuard sortGuard;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final WorkspaceInviteRepository inviteRepository;
    private final WorkspaceUserRepository workspaceUserRepository;
    private final WorkspaceInviteMapper inviteMapper;
    private final NotificationService notificationService;


    // MASTER 만 초대 가능. 자기 초대·이미 멤버·대기 중 초대 중복을 막고 알림을 보낸다
    @Override
    @Transactional
    public void inviteUserToWorkspace(Long inviterId, Long workspaceId, InviteRequestDto dto) {
        // MASTER 권한 확인
        taskAuthorityUtil.validateWorkspaceMaster(workspaceId, inviterId);

        if (inviterId.equals(dto.getUserId())) {
            throw new BusinessException(InviteResponse.INVITE_SELF);
        }

        if (workspaceUserRepository.existsById(new WorkspaceUserPk(workspaceId, dto.getUserId()))) {
            throw new BusinessException(WorkspaceUserResponse.USER_ALREADY_MEMBER);
        }

        if (inviteRepository.existsByWorkspaceIdAndInviteeIdAndStatus(workspaceId, dto.getUserId(),
                InviteStatus.PENDING)) {
            throw new BusinessException(InviteResponse.INVITE_ALREADY_PENDING);
        }

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(
                        () -> new BusinessException(CommonResponse.WORKSPACE_NOT_FOUND));
        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new BusinessException(CommonResponse.USER_NOT_FOUND));
        User invitee = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new BusinessException(CommonResponse.USER_NOT_FOUND));

        WorkspaceInvite invite = inviteMapper.mapToEntity(workspace, inviter, invitee, dto);
        WorkspaceInvite savedInvite = inviteRepository.save(invite);

        // 초대받은 사용자에게 알림 전송
        String message = String.format("'%s' 워크스페이스에 초대되었습니다.", workspace.getWorkspaceName());
        notificationService.createNotification(dto.getUserId(),
                NotificationType.WORKSPACE_INVITED, message, savedInvite.getId());
    }

    // 대기 중(PENDING) 초대만 돌려준다
    @Override
    public PageResponseDto<InviteResponseDto> getMyInvites(Long inviteeId, Pageable pageable) {
        return PageResponseDto.of(
                inviteRepository.findAllByInviteeIdAndStatus(inviteeId, InviteStatus.PENDING,
                        sortGuard.apply(pageable, WorkspaceInvite.class)),
                inviteMapper::mapToDto);
    }

    // 수락 시 초대에 담긴 역할로 멤버가 된다. 이미 처리된 초대는 거부한다
    @Override
    @Transactional
    public void acceptInvite(Long inviteId, Long inviteeId) {
        WorkspaceInvite invite = inviteRepository.findByIdAndInviteeId(inviteId, inviteeId)
                .orElseThrow(() -> new BusinessException(InviteResponse.INVITE_NOT_FOUND));

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new BusinessException(InviteResponse.INVITE_ALREADY_PROCESSED);
        }

        invite.accept();

        WorkspaceUser workspaceUser = WorkspaceUser.builder()
                .id(new WorkspaceUserPk(invite.getWorkspace().getId(), inviteeId))
                .user(invite.getInvitee())
                .workspace(invite.getWorkspace())
                .role(invite.getRole())
                .build();

        workspaceUserRepository.save(workspaceUser);
    }

    // 본인에게 온 초대인지 조회 단계에서 함께 확인한다
    @Override
    @Transactional
    public void rejectInvite(Long inviteId, Long inviteeId) {
        WorkspaceInvite invite = inviteRepository.findByIdAndInviteeId(inviteId, inviteeId)
                .orElseThrow(() -> new BusinessException(InviteResponse.INVITE_NOT_FOUND));

        invite.reject();
        inviteRepository.save(invite);
    }

    private WorkspaceUser getWorkspaceUserOrThrow(Long workspaceId, Long userId) {
        return workspaceUserRepository.findById(new WorkspaceUserPk(workspaceId, userId))
                .orElseThrow(() -> new BusinessException(CommonResponse.USER_NOT_FOUND));
    }

}