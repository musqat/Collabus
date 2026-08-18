package com.muscat.Collabus.Todo.repository;

import com.muscat.Collabus.Todo.entity.Todo;
import com.muscat.Collabus.enums.status.TodoStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long> {

  List<Todo> findAllByTaskId(Long taskId);

  @EntityGraph(attributePaths = {"assignee"})
  Page<Todo> findAllByTaskId(Long taskId, Pageable pageable);


  @EntityGraph(attributePaths = {"assignee"})
  Page<Todo> findAllByTaskIdAndStatus(Long taskId, TodoStatus status, Pageable pageable);
}
