package com.muscat.Collabus.WorkspaceUser.repository;

import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUser;
import com.muscat.Collabus.enums.role.WorkspaceRole;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUserPk;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceUserRepository extends JpaRepository<WorkspaceUser, WorkspaceUserPk> {

    // 워크스페이스의 전체 멤버. 마스터 승계 대상을 고를 때 쓴다
    List<WorkspaceUser> findAllById_WorkspaceId(Long workspaceId);

    // 멤버 목록 화면용. 이름을 함께 보여주므로 사용자까지 가져온다
    @EntityGraph(attributePaths = {"user"})
    Page<WorkspaceUser> findAllById_WorkspaceId(Long workspaceId, Pageable pageable);

    // 탈퇴 처리에서 참여 중인 워크스페이스를 전부 순회한다
    List<WorkspaceUser> findAllById_UserId(Long userId);

    // 내가 참여 중인 워크스페이스 목록. 워크스페이스와 생성자까지 함께 가져온다
    @EntityGraph(attributePaths = {"workspace", "workspace.founder"})
    Page<WorkspaceUser> findAllById_UserId(Long userId, Pageable pageable);


    // 워크스페이스 참여자인지 확인
    boolean existsById_WorkspaceIdAndId_UserId(Long workspaceId, Long userId);

    // 탈퇴 처리에서 Task 매니저를 넘길 대상을 찾는다
    @EntityGraph(attributePaths = {"user"})
    Optional<WorkspaceUser> findFirstById_WorkspaceIdAndRole(Long workspaceId, WorkspaceRole role);

    // 역할까지 맞는 멤버가 있는지만 확인한다. 엔티티를 로드하지 않는다
    boolean existsById_WorkspaceIdAndId_UserIdAndRole(Long workspaceId, Long userId, WorkspaceRole role);

    // 역할이 주어진 목록 안에 드는 멤버가 있는지 확인한다
    boolean existsById_WorkspaceIdAndId_UserIdAndRoleIn(Long workspaceId, Long userId,
        Collection<WorkspaceRole> roles);

}
