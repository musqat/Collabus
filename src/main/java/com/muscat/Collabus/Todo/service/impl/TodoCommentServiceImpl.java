package com.muscat.Collabus.Todo.service.impl;

import com.muscat.Collabus.Notification.service.NotificationService;
import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Task.entity.TaskUser;
import com.muscat.Collabus.Task.repository.TaskUserRepository;
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
import com.muscat.Collabus.enums.NotificationType;
import com.muscat.Collabus.enums.response.CommonResponse;
import com.muscat.Collabus.enums.response.TodoResponse;
import com.muscat.Collabus.enums.role.TaskRole;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoCommentServiceImpl implements TodoCommentService {

  private final TodoRepository todoRepository;
  private final UserRepository userRepository;
  private final TodoCommentRepository commentRepository;
  private final TodoCommentMapper commentMapper;
  private final ParticipantUtil participantUtil;
  private final NotificationService notificationService;
  private final TaskUserRepository taskUserRepository;

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

    TodoComment savedComment = commentRepository.save(comment);

    // 알림을 받을 사용자 목록 (중복 제거)
    Set<Long> notifyUserIds = new HashSet<>();

    // todo 담당자에게 알림 (작성자 본인 제외)
    if (todo.getAssignee() != null && !todo.getAssignee().getId().equals(userId)) {
      notifyUserIds.add(todo.getAssignee().getId());
    }

    // Task Manager 전원에게 알림 (작성자 본인 제외)
    Task task = todo.getTask();
    taskUserRepository.findByTaskAndRole(task, TaskRole.MANAGER).stream()
        .map(TaskUser::getUser)
        .map(User::getId)
        .filter(managerId -> !managerId.equals(userId))
        .forEach(notifyUserIds::add);

    // 알림 전송
    String message = String.format("'%s' 할일에 새 댓글이 추가되었습니다.", todo.getTitle());
    for (Long notifyUserId : notifyUserIds) {
      notificationService.createNotification(notifyUserId,
          NotificationType.COMMENT_ADDED, message, todoId);
    }

    return commentMapper.mapToDto(savedComment);
  }

  @Override
  @Transactional
  public TodoCommentDto updateComment(Long commentId, String content, Long userId) {
    TodoComment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new ResourceNotFoundException(TodoResponse.COMMENT_NOT_FOUND));

    if (!comment.getAuthor().getId().equals(userId)) {
      throw new BusinessException(CommonResponse.UNAUTHORIZED);
    }

    comment.updateContent(content);
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
