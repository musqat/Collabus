package com.muscat.Collabus.Todo.repository;

import com.muscat.Collabus.Todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TodoRepository extends JpaRepository<Todo, Long>,
    JpaSpecificationExecutor<Todo> {

  // Todo 목록. 담당자를 함께 표시하므로 한 번에 가져온다
  @Override
  @EntityGraph(attributePaths = {"assignee"})
  Page<Todo> findAll(Specification<Todo> spec, Pageable pageable);
}
