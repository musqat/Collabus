package com.muscat.Collabus.WorkspaceUser.service;

import com.muscat.Collabus.WorkspaceUser.model.InviteRequestDto;
import com.muscat.Collabus.WorkspaceUser.model.InviteResponseDto;
import com.muscat.Collabus.common.dto.PageResponseDto;
import org.springframework.data.domain.Pageable;

public interface WorkspaceUserInviteService {

  // 워크스페이스에 사용자 초대
  void inviteUserToWorkspace(Long inviterId, Long workspaceId, InviteRequestDto dto);

  // 현재 로그인 유저 기준으로 받은 초대 목록 조회
  PageResponseDto<InviteResponseDto> getMyInvites(Long inviteeId, Pageable pageable);

  // 초대 수락
  void acceptInvite(Long inviteId, Long inviteeId);

  // 초대 거절
  void rejectInvite(Long inviteId, Long inviteeId);
}
