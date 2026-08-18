package com.muscat.Collabus.Todo.repository;

import com.muscat.Collabus.Todo.entity.TodoComment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoCommentRepository extends JpaRepository<TodoComment, Long> {

    // 댓글마다 작성자를 표시하므로 함께 가져온다
    @EntityGraph(attributePaths = {"author"})
    Page<TodoComment> findAllByTodoId(Long todoId, Pageable pageable);

}
