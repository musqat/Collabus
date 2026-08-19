package com.muscat.Collabus.WorkspaceUser.service.impl;

import com.muscat.Collabus.common.util.SortGuard;
import com.muscat.Collabus.common.dto.PageResponseDto;
import org.springframework.data.domain.Pageable;
import com.muscat.Collabus.Workspace.repository.WorkspaceRepository;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUser;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUserPk;
import com.muscat.Collabus.WorkspaceUser.mapper.WorkspaceUserMapper;
import com.muscat.Collabus.WorkspaceUser.model.WorkspaceUserResponseDto;
import com.muscat.Collabus.WorkspaceUser.repository.WorkspaceUserRepository;
import com.muscat.Collabus.WorkspaceUser.service.WorkspaceUserService;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.common.exception.ResourceNotFoundException;
import com.muscat.Collabus.common.util.ParticipantUtil;
import com.muscat.Collabus.enums.response.CommonResponse;
import com.muscat.Collabus.enums.response.WorkspaceUserResponse;
import com.muscat.Collabus.enums.role.WorkspaceRole;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceUserServiceImpl implements WorkspaceUserService {

    private final SortGuard sortGuard;
    private final WorkspaceUserRepository workspaceUserRepository;
    private final WorkspaceUserMapper workspaceUserMapper;
    private final ParticipantUtil participantUtil;
    private final WorkspaceRepository workspaceRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<WorkspaceUserResponseDto> getUsersInWorkspace(Long workspaceId,
                                                                         Long userId, Pageable pageable) {
        participantUtil.validateWorkspaceParticipant(workspaceId, userId);
        return PageResponseDto.of(
                workspaceUserRepository.findAllById_WorkspaceId(workspaceId,
                        sortGuard.apply(pageable, WorkspaceUser.class)),
                workspaceUserMapper::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceUserResponseDto> getMyJoinedWorkspaces(Long userId) {
        return workspaceUserRepository.findAllById_UserId(userId).stream()
                .map(workspaceUserMapper::mapToDto)
                .collect(Collectors.toList());
    }

    // MASTER 만 변경 가능하고 자기 역할은 못 바꾼다. MASTER 로 올리면 기존 MASTER 는 MANAGER 로 내려간다
    @Override
    @Transactional
    public void updateUserRole(Long workspaceId, Long targetUserId, WorkspaceRole newRole, Long actorId) {
        participantUtil.validateWorkspaceParticipant(workspaceId, actorId);
        checkPermission(workspaceId, actorId, WorkspaceRole.MASTER);

        if (targetUserId.equals(actorId)) {
            throw new BusinessException(CommonResponse.CANNOT_CHANGE_SELF_ROLE);
        }

        WorkspaceUser target = getWorkspaceUserOrThrow(workspaceId, targetUserId);

        if (newRole == WorkspaceRole.MASTER) {
            WorkspaceUser currentMaster = getWorkspaceUserOrThrow(workspaceId, actorId);
            currentMaster.changeRole(WorkspaceRole.MANAGER);
            workspaceUserRepository.save(currentMaster);
        }

        target.changeRole(newRole);
        workspaceUserRepository.save(target);
    }

    // MASTER 만 제거 가능. 자기 자신은 제거할 수 없다 (탈퇴를 쓴다)
    @Override
    @Transactional
    public void removeUser(Long workspaceId, Long userId, Long actorId) {
        checkPermission(workspaceId, actorId, WorkspaceRole.MASTER);

        if (userId.equals(actorId)) {
            throw new BusinessException(CommonResponse.CANNOT_REMOVE_SELF);
        }

        WorkspaceUser user = getWorkspaceUserOrThrow(workspaceId, userId);
        workspaceUserRepository.delete(user);
    }

    // 마지막 멤버가 나가면 워크스페이스까지 삭제하고, MASTER 가 나가면 남은 멤버 중 최상위 역할자가 승계한다
    @Override
    @Transactional
    public void leaveWorkspace(Long workspaceId, Long userId) {
        WorkspaceUser user = getWorkspaceUserOrThrow(workspaceId, userId);
        List<WorkspaceUser> members = workspaceUserRepository.findAllById_WorkspaceId(workspaceId);

        if (members.size() == 1) {
            workspaceUserRepository.delete(user);
            workspaceRepository.deleteById(workspaceId);
            return;
        }

        if (user.getRole() == WorkspaceRole.MASTER) {
            WorkspaceUser newMaster = members.stream()
                    .filter(u -> !u.getId().getUserId().equals(userId))
                    .min(Comparator.comparing(u -> u.getRole().ordinal()))
                    .orElseThrow(() -> new BusinessException(WorkspaceUserResponse.NOT_FOUND_NEXT_MASTER));

            newMaster.changeRole(WorkspaceRole.MASTER);
            workspaceUserRepository.save(newMaster);
        }

        workspaceUserRepository.delete(user);
    }

    private WorkspaceUser getWorkspaceUserOrThrow(Long workspaceId, Long userId) {
        return workspaceUserRepository.findById(new WorkspaceUserPk(workspaceId, userId))
                .orElseThrow(() -> new ResourceNotFoundException(CommonResponse.USER_NOT_FOUND));
    }

    private void checkPermission(Long workspaceId, Long userId, WorkspaceRole... allowedRoles) {
        WorkspaceUser user = getWorkspaceUserOrThrow(workspaceId, userId);
        Set<WorkspaceRole> allowed = Set.of(allowedRoles);
        if (!allowed.contains(user.getRole())) {
            throw new AccessDeniedException(CommonResponse.UNAUTHORIZED.getMessage());
        }
    }
}
