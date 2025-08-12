package com.muscat.Collabus.Todo.service.impl;

import com.muscat.Collabus.Todo.entity.Todo;
import com.muscat.Collabus.Todo.entity.TodoComment;
import com.muscat.Collabus.Todo.mapper.TodoCommentMapper;
import com.muscat.Collabus.Todo.model.TodoCommentDto;
import com.muscat.Collabus.Todo.repository.TodoCommentRepository;
import com.muscat.Collabus.Todo.repository.TodoRepository;
import com.muscat.Collabus.Todo.service.TodoCommentService;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.common.exception.ResourceNotFoundException;
import com.muscat.Collabus.common.util.ParticipantUtil;
import com.muscat.Collabus.enums.response.CommonResponse;
import com.muscat.Collabus.enums.response.TodoResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TodoCommentServiceImpl implements TodoCommentService {

  private final TodoRepository todoRepository;
  private final UserRepository userRepository;
  private final TodoCommentRepository commentRepository;
  private final TodoCommentMapper commentMapper;
  private final ParticipantUtil participantUtil;

  @Override
  @Transactional
  public TodoCommentDto addComment(Long todoId, String content, Long userId) {
    Todo todo = todoRepository.findById(todoId)
        .orElseThrow(() -> new ResourceNotFoundException(CommonResponse.TODO_NOT_FOUND));

    participantUtil.validateTaskParticipant(todo.getTask().getId(), userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException(CommonResponse.USER_NOT_FOUND));

    TodoComment comment = TodoComment.builder()
        .todo(todo)
        .author(user)
        .content(content)
        .build();

    return commentMapper.mapToDto(commentRepository.save(comment));
  }

  @Override
  @Transactional
  public TodoCommentDto updateComment(Long commentId, String content, Long userId) {
    TodoComment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new ResourceNotFoundException(TodoResponse.COMMENT_NOT_FOUND));

    if (!comment.getAuthor().getId().equals(userId)) {
      throw new BusinessException(CommonResponse.UNAUTHORIZED);
    }

    comment.setContent(content);
    return commentMapper.mapToDto(commentRepository.save(comment));
  }

  @Override
  @Transactional
  public void deleteComment(Long commentId, Long userId) {
    TodoComment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new ResourceNotFoundException(TodoResponse.COMMENT_NOT_FOUND));

    if (!comment.getAuthor().getId().equals(userId)) {
      throw new BusinessException(CommonResponse.UNAUTHORIZED);
    }

    commentRepository.delete(comment);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TodoCommentDto> getComments(Long todoId) {
    return commentRepository.findAllByTodoId(todoId).stream()
        .map(commentMapper::mapToDto)
        .toList();
  }
}
