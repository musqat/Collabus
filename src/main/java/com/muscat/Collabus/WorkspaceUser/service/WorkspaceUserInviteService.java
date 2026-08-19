package com.muscat.Collabus.WorkspaceUser.service;

import com.muscat.Collabus.WorkspaceUser.model.InviteRequestDto;
import com.muscat.Collabus.WorkspaceUser.model.InviteResponseDto;
import com.muscat.Collabus.common.dto.PageResponseDto;
import org.springframework.data.domain.Pageable;

public interface WorkspaceUserInviteService {

  // 워크스페이스에 사용자를 초대한다
  void inviteUserToWorkspace(Long inviterId, Long workspaceId, InviteRequestDto dto);

  // 받은 초대 목록. 대기 중인 것만 돌려준다
  PageResponseDto<InviteResponseDto> getMyInvites(Long inviteeId, Pageable pageable);

  // 초대를 수락한다. 초대에 담긴 역할로 멤버가 된다
  void acceptInvite(Long inviteId, Long inviteeId);

  // 초대를 거절한다
  void rejectInvite(Long inviteId, Long inviteeId);
}
