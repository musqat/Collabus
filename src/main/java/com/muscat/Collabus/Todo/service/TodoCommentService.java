package com.muscat.Collabus.Todo.service;

import com.muscat.Collabus.Todo.model.TodoCommentDto;
import java.util.List;

public interface TodoCommentService {

  TodoCommentDto addComment(Long todoId, String content, Long userId);

  List<TodoCommentDto> getComments(Long todoId);

  TodoCommentDto updateComment(Long commentId, String content, Long userId);

  void deleteComment(Long commentId, Long userId);
}
