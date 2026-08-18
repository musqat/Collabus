package com.muscat.Collabus.Task.repository;

import com.muscat.Collabus.Task.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @EntityGraph(attributePaths = {"taskManager"})
    Page<Task> findAllByWorkspace_Id(Long workspaceId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Task t WHERE t.workspace.id = :workspaceId")
    void deleteAllByWorkspaceId(@Param("workspaceId") Long workspaceId);
}
