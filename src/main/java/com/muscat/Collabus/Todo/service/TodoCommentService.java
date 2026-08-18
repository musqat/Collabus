package com.muscat.Collabus.Todo.service;

import com.muscat.Collabus.common.dto.PageResponseDto;
import org.springframework.data.domain.Pageable;
import com.muscat.Collabus.Todo.model.TodoCommentDto;

public interface TodoCommentService {

    TodoCommentDto addComment(Long todoId, String content, Long userId);

    PageResponseDto<TodoCommentDto> getComments(Long todoId, Pageable pageable);

    TodoCommentDto updateComment(Long commentId, String content, Long userId);

    void deleteComment(Long commentId, Long userId);
}
