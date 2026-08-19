package com.muscat.Collabus.WorkspaceUser.repository;

import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceInvite;
import com.muscat.Collabus.enums.status.InviteStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceInviteRepository extends JpaRepository<WorkspaceInvite, Long> {

  // 상태별 초대 목록. 초대자와 워크스페이스를 함께 가져온다
  @EntityGraph(attributePaths = {"inviter", "workspace"})
  Page<WorkspaceInvite> findAllByInviteeIdAndStatus(Long inviteeId, InviteStatus status,
      Pageable pageable);

  // 초대 중복 방지
  boolean existsByWorkspaceIdAndInviteeIdAndStatus(Long workspaceId, Long inviteeId, InviteStatus status);

  // 탈퇴 시 주고받은 초대를 지운다
  void deleteByInviteeIdOrInviterId(Long inviteeId, Long inviterId);

  // 수락·거절 시 본인에게 온 초대인지 함께 확인한다
  Optional<WorkspaceInvite> findByIdAndInviteeId(Long inviteId, Long inviteeId);

}
