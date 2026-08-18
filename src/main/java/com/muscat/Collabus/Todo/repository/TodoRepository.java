package com.muscat.Collabus.Todo.repository;

import com.muscat.Collabus.Todo.entity.Todo;
import com.muscat.Collabus.enums.status.TodoStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TodoRepository extends JpaRepository<Todo, Long> {

  List<Todo> findAllByTaskId(Long taskId);

  Page<Todo> findAllByTaskId(Long taskId, Pageable pageable);

  List<Todo> findAllByTaskIdAndStatus(Long taskId, TodoStatus status);

  Page<Todo> findAllByTaskIdAndStatus(Long taskId, TodoStatus status, Pageable pageable);

  @Modifying
  @Query("DELETE FROM Todo t WHERE t.task.workspace.id = :workspaceId")
  void deleteAllByWorkspaceId(@Param("workspaceId") Long workspaceId);
}
