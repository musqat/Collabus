package com.muscat.Collabus.Todo.repository;

import com.muscat.Collabus.Todo.entity.TodoComment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TodoCommentRepository extends JpaRepository<TodoComment, Long> {

    @EntityGraph(attributePaths = {"author"})
    Page<TodoComment> findAllByTodoId(Long todoId, Pageable pageable);

    void deleteAllByTodoId(Long todoId);

    @Modifying
    @Query("DELETE FROM TodoComment c WHERE c.todo.task.workspace.id = :workspaceId")
    void deleteAllByWorkspaceId(@Param("workspaceId") Long workspaceId);
}
