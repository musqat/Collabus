package com.muscat.Collabus.Task.repository;

import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Workspace.entity.Workspace;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

  List<Task> findAllByWorkspace(Workspace workspace);

  @EntityGraph(attributePaths = {"taskManager"})
  List<Task> findAllByWorkspace_Id(Long workspaceId);

  @Modifying
  @Query("DELETE FROM Task t WHERE t.workspace.id = :workspaceId")
  void deleteAllByWorkspaceId(@Param("workspaceId") Long workspaceId);
}
