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

    // 워크스페이스 탈퇴 시 마스터 승계 대상을 고르려면 전체 멤버가 필요하다
    List<WorkspaceUser> findAllById_WorkspaceId(Long workspaceId);

    // 멤버 목록 화면용. 이름을 함께 보여주므로 사용자까지 가져온다
    @EntityGraph(attributePaths = {"user"})
    Page<WorkspaceUser> findAllById_WorkspaceId(Long workspaceId, Pageable pageable);

    // 내가 참여 중인 워크스페이스 목록. 워크스페이스와 생성자까지 함께 가져온다
    @EntityGraph(attributePaths = {"workspace", "workspace.founder"})
    List<WorkspaceUser> findAllById_UserId(Long userId);

    // 멤버 한 명의 참여 정보 (역할 변경·탈퇴에 사용)
    Optional<WorkspaceUser> findById_WorkspaceIdAndId_UserId(Long workspaceId, Long userId);

    // 워크스페이스 참여자인지 확인
    boolean existsById_WorkspaceIdAndId_UserId(Long workspaceId, Long userId);

    // 역할 확인만 필요할 때. 엔티티를 영속성 컨텍스트에 올리지 않는다.
    boolean existsById_WorkspaceIdAndId_UserIdAndRole(Long workspaceId, Long userId, WorkspaceRole role);

    // 허용 역할이 여럿일 때 (예: Task 전체 조회는 MASTER·MANAGER 만)
    boolean existsById_WorkspaceIdAndId_UserIdAndRoleIn(Long workspaceId, Long userId,
        Collection<WorkspaceRole> roles);

}
