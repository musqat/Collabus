package com.muscat.Collabus.WorkspaceUser.service;

import com.muscat.Collabus.common.dto.PageResponseDto;
import org.springframework.data.domain.Pageable;
import com.muscat.Collabus.WorkspaceUser.model.WorkspaceUserResponseDto;
import com.muscat.Collabus.enums.role.WorkspaceRole;

public interface WorkspaceUserService {

    // 워크스페이스에 속한 모든 사용자 조회
    PageResponseDto<WorkspaceUserResponseDto> getUsersInWorkspace(Long workspaceId, Long userId, Pageable pageable);

    // 내가 속한 모든 워크스페이스 조회
    PageResponseDto<WorkspaceUserResponseDto> getMyJoinedWorkspaces(Long userId,
        Pageable pageable);

    // 구성원 역할 변경
    void updateUserRole(Long workspaceId, Long targetUserId, WorkspaceRole newRole, Long actorId);

    // 워크스페이스에서 특정 유저 제거 (권한 확인 필요)
    void removeUser(Long workspaceId, Long userId, Long actorId);

    // 본인이 워크스페이스 나가기
    void leaveWorkspace(Long workspaceId, Long userId);

}
