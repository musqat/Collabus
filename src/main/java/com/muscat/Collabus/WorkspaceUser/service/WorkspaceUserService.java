package com.muscat.Collabus.WorkspaceUser.service;

import com.muscat.Collabus.common.dto.PageResponseDto;
import org.springframework.data.domain.Pageable;
import com.muscat.Collabus.WorkspaceUser.model.WorkspaceUserResponseDto;
import com.muscat.Collabus.enums.role.WorkspaceRole;

public interface WorkspaceUserService {

    // 워크스페이스 멤버 목록
    PageResponseDto<WorkspaceUserResponseDto> getUsersInWorkspace(Long workspaceId, Long userId, Pageable pageable);

    // 멤버 역할을 바꾼다. MASTER 로 올리면 기존 MASTER 는 MANAGER 로 내려간다
    void updateUserRole(Long workspaceId, Long targetUserId, WorkspaceRole newRole, Long actorId);

    // 워크스페이스에서 멤버를 제거한다
    void removeUser(Long workspaceId, Long userId, Long actorId);

    // 워크스페이스에서 나간다. 마지막 멤버면 워크스페이스까지 지우고, MASTER 면 남은 멤버 중 최상위 역할자가 승계한다
    void leaveWorkspace(Long workspaceId, Long userId);

}
