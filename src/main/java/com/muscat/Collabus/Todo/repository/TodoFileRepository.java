package com.muscat.Collabus.Todo.repository;

import com.muscat.Collabus.Todo.entity.TodoFile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TodoFileRepository extends JpaRepository<TodoFile, Long> {

    List<TodoFile> findAllByWorkId(Long workId);

    void deleteAllByWorkId(Long workId);

    @Modifying
    @Query("DELETE FROM TodoFile f WHERE f.work.todo.id = :todoId")
    void deleteAllByTodoId(@Param("todoId") Long todoId);

    @Modifying
    @Query("DELETE FROM TodoFile f WHERE f.work.todo.task.workspace.id = :workspaceId")
    void deleteAllByWorkspaceId(@Param("workspaceId") Long workspaceId);
}
