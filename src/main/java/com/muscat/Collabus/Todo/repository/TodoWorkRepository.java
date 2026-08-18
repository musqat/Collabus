package com.muscat.Collabus.Todo.repository;

import com.muscat.Collabus.Todo.entity.TodoWork;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TodoWorkRepository extends JpaRepository<TodoWork, Long> {

  @EntityGraph(attributePaths = {"author"})
  List<TodoWork> findAllByTodoId(Long todoId);

  void deleteAllByTodoId(Long todoId);

  @Modifying
  @Query("DELETE FROM TodoWork w WHERE w.todo.task.workspace.id = :workspaceId")
  void deleteAllByWorkspaceId(@Param("workspaceId") Long workspaceId);
}
