package com.muscat.Collabus.Workspace.repository;

import com.muscat.Collabus.Workspace.entity.Workspace;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

  @EntityGraph(attributePaths = {"founder"})
  List<Workspace> findAllByFounderId(Long userId);
}
