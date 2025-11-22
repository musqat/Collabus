package com.muscat.Collabus.Todo.repository;

import com.muscat.Collabus.Todo.entity.TodoWork;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoWorkRepository extends JpaRepository<TodoWork, Long> {

  List<TodoWork> findAllByTodoId(Long todoId);

  void deleteAllByTodoId(Long todoId);
}
