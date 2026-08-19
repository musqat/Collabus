package com.muscat.Collabus.Todo.service;

import com.muscat.Collabus.common.dto.PageResponseDto;
import org.springframework.data.domain.Pageable;
import com.muscat.Collabus.Todo.model.TodoCommentDto;

public interface TodoCommentService {

    // 댓글을 단다
    TodoCommentDto addComment(Long todoId, String content, Long userId);

    // Todo 의 댓글 목록
    PageResponseDto<TodoCommentDto> getComments(Long todoId, Long requesterId,
        Pageable pageable);

    // 댓글을 수정한다
    TodoCommentDto updateComment(Long commentId, String content, Long userId);

    // 댓글을 삭제한다
    void deleteComment(Long commentId, Long userId);
}
