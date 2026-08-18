package com.muscat.Collabus.Todo.repository;

import com.muscat.Collabus.Todo.entity.TodoWork;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoWorkRepository extends JpaRepository<TodoWork, Long> {

    // Task/Todo 삭제 시 하위 작업을 전부 순회해야 하므로 페이징하지 않는다
    List<TodoWork> findAllByTodoId(Long todoId);

    // 목록에서 작성자를 표시하므로 함께 가져온다
    @EntityGraph(attributePaths = {"author"})
    Page<TodoWork> findAllByTodoId(Long todoId, Pageable pageable);

}
