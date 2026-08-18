package com.muscat.Collabus.Todo.repository;

import com.muscat.Collabus.Todo.entity.TodoWork;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TodoWorkRepository extends JpaRepository<TodoWork, Long> {

    // Task/Todo 삭제 시 하위 작업을 전부 순회해야 하므로 페이징하지 않는다
    List<TodoWork> findAllByTodoId(Long todoId);

    @EntityGraph(attributePaths = {"author"})
    Page<TodoWork> findAllByTodoId(Long todoId, Pageable pageable);

    void deleteAllByTodoId(Long todoId);

    @Modifying
    @Query("DELETE FROM TodoWork w WHERE w.todo.task.workspace.id = :workspaceId")
    void deleteAllByWorkspaceId(@Param("workspaceId") Long workspaceId);
}
