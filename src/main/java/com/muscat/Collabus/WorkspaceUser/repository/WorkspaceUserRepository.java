package com.muscat.Collabus.WorkspaceUser.repository;

import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUser;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUserPk;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceUserRepository extends JpaRepository<WorkspaceUser, WorkspaceUserPk> {

  List<WorkspaceUser> findAllById_WorkspaceId(Long workspaceId);

  List<WorkspaceUser> findAllById_UserId(Long userId);

  boolean existsById(WorkspaceUserPk id);

  boolean existsById_WorkspaceIdAndId_UserId(Long workspaceId, Long userId);

  void deleteAllByWorkspaceId(Long workspaceId);
}
