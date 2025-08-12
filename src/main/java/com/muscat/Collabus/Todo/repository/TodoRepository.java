package com.muscat.Collabus.Todo.repository;

import com.muscat.Collabus.Todo.entity.Todo;
import com.muscat.Collabus.enums.status.TodoStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long> {
  List<Todo> findAllByTaskId(Long taskId);
  List<Todo> findAllByTaskIdAndStatus(Long taskId, TodoStatus status);

}
