package com.muscat.Collabus.Todo.repository;

import com.muscat.Collabus.Todo.entity.Todo;
import com.muscat.Collabus.enums.status.TodoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long> {

  // Task 의 Todo 목록. 담당자를 함께 표시하므로 한 번에 가져온다
  @EntityGraph(attributePaths = {"assignee"})
  Page<Todo> findAllByTaskId(Long taskId, Pageable pageable);

  // 상태 필터가 붙은 목록
  @EntityGraph(attributePaths = {"assignee"})
  Page<Todo> findAllByTaskIdAndStatus(Long taskId, TodoStatus status, Pageable pageable);
}
