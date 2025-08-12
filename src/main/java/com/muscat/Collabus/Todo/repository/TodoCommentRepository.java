package com.muscat.Collabus.Todo.repository;

import com.muscat.Collabus.Todo.entity.TodoComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoCommentRepository extends JpaRepository<TodoComment, Long> {
  List<TodoComment> findAllByTodoId(Long todoId);
}
