package com.muscat.Collabus.WorkspaceUser.repository;

import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceInvite;
import com.muscat.Collabus.enums.status.InviteStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceInviteRepository extends JpaRepository<WorkspaceInvite, Long> {

  // 특정 유저가 받은 모든 초대
  @EntityGraph(attributePaths = {"inviter", "workspace"})
  List<WorkspaceInvite> findAllByInviteeIdAndStatus(Long inviteeId, InviteStatus status);

  // 초대 중복 방지
  boolean existsByWorkspaceIdAndInviteeIdAndStatus(Long workspaceId, Long inviteeId, InviteStatus status);

  // 수락·거절 시 본인에게 온 초대인지 함께 확인한다
  Optional<WorkspaceInvite> findByIdAndInviteeId(Long inviteId, Long inviteeId);

}
