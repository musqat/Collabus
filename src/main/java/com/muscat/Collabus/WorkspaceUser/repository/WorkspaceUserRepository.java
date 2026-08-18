package com.muscat.Collabus.WorkspaceUser.repository;

import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUser;
import com.muscat.Collabus.enums.role.WorkspaceRole;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUserPk;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceUserRepository extends JpaRepository<WorkspaceUser, WorkspaceUserPk> {

    // 워크스페이스 탈퇴 시 마스터 승계 대상을 고르려면 전체 멤버가 필요하다
    List<WorkspaceUser> findAllById_WorkspaceId(Long workspaceId);

    @EntityGraph(attributePaths = {"user"})
    Page<WorkspaceUser> findAllById_WorkspaceId(Long workspaceId, Pageable pageable);

    @EntityGraph(attributePaths = {"workspace", "workspace.founder"})
    List<WorkspaceUser> findAllById_UserId(Long userId);

    Optional<WorkspaceUser> findById_WorkspaceIdAndId_UserId(Long workspaceId, Long userId);

    boolean existsById(WorkspaceUserPk id);

    boolean existsById_WorkspaceIdAndId_UserId(Long workspaceId, Long userId);

  // 역할 확인만 필요할 때. 엔티티를 영속성 컨텍스트에 올리지 않는다.
  boolean existsById_WorkspaceIdAndId_UserIdAndRole(Long workspaceId, Long userId, WorkspaceRole role);

}
