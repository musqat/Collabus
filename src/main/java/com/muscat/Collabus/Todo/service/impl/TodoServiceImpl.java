package com.muscat.Collabus.Todo.service.impl;

import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Todo.entity.Todo;
import com.muscat.Collabus.Todo.mapper.TodoMapper;
import com.muscat.Collabus.Todo.model.TodoRequestDto;
import com.muscat.Collabus.Todo.model.TodoResponseDto;
import com.muscat.Collabus.Todo.repository.TodoRepository;
import com.muscat.Collabus.Todo.service.TodoService;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.common.util.EntityFinderUtil;
import com.muscat.Collabus.common.util.ParticipantUtil;
import com.muscat.Collabus.common.util.TaskAuthorityUtil;
import com.muscat.Collabus.enums.response.TodoResponse;
import com.muscat.Collabus.enums.status.TodoStatus;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

  private final TodoRepository todoRepository;
  private final TodoMapper todoMapper;
  private final ParticipantUtil participantUtil;
  private final TaskAuthorityUtil taskAuthorityUtil;
  private final EntityFinderUtil finder;

  @Override
  @Transactional
  public TodoResponseDto createTodo(TodoRequestDto dto, Long creatorId) {
    Task task = finder.findTaskById(dto.getTaskId());
    participantUtil.validateTaskParticipant(task.getId(), creatorId);

    validateDueDate(dto.getDueDate(), task);

    User assignee = finder.findUserById(dto.getAssigneeId());
    Todo todo = todoMapper.mapToEntity(dto, task, assignee);
    return todoMapper.mapToDto(todoRepository.save(todo));
  }

  @Override
  @Transactional
  public TodoResponseDto updateTodo(Long todoId, TodoRequestDto dto, Long updaterId) {
    Todo todo = finder.findTodoById(todoId);
    validateManagerAuthority(todo.getTask(), updaterId);

    todoMapper.updateFromDto(dto, todo);
    return todoMapper.mapToDto(todoRepository.save(todo));
  }

  @Override
  @Transactional
  public void deleteTodo(Long todoId, Long userId) {
    Todo todo = finder.findTodoById(todoId);
    validateManagerAuthority(todo.getTask(), userId);
    todoRepository.delete(todo);
  }

  @Override
  public TodoResponseDto getTodoById(Long todoId) {
    return todoMapper.mapToDto(finder.findTodoById(todoId));
  }

  @Override
  public List<TodoResponseDto> getTodosByTask(Long taskId, String status) {
    List<Todo> todos = (status != null)
        ? todoRepository.findAllByTaskIdAndStatus(taskId, TodoStatus.valueOf(status))
        : todoRepository.findAllByTaskId(taskId);

    return todos.stream().map(todoMapper::mapToDto).toList();
  }

  @Override
  @Transactional
  public void completeOwnTodo(Long todoId, Long userId) {
    Todo todo = finder.findTodoById(todoId);
    participantUtil.validateTaskParticipant(todo.getTask().getId(), userId);
    validateAssignee(todo, userId);

    todo.setDone(true);
    todo.setDoneAt(LocalDateTime.now());
    todo.setStatus(TodoStatus.WAITING_REVIEW);
    todoRepository.save(todo);
  }

  @Override
  @Transactional
  public TodoResponseDto confirmTodoCompletion(Long todoId, Long taskManagerId) {
    Todo todo = finder.findTodoById(todoId);
    taskAuthorityUtil.validateTaskManager(todo.getTask(), taskManagerId);

    if (todo.getStatus() != TodoStatus.WAITING_REVIEW) {
      throw new BusinessException(TodoResponse.NEED_WAITING_REVIEW_STATUS);
    }

    todo.setStatus(TodoStatus.CONFIRMED);
    return todoMapper.mapToDto(todoRepository.save(todo));
  }

  @Override
  @Transactional
  public void changeAssignee(Long todoId, Long newAssigneeId, Long managerId) {
    Todo todo = finder.findTodoById(todoId);
    Task task = todo.getTask();

    // 권한 검증: Task Manager만 담당자 변경 가능
    taskAuthorityUtil.validateTaskManager(task, managerId);

    // 같은 사람으로 변경 불가
    if (todo.getAssignee() != null && todo.getAssignee().getId().equals(newAssigneeId)) {
      throw new BusinessException(TodoResponse.ALREADY_ASSIGNED_TO_USER);
    }

    // Task 참여자 확인
    participantUtil.validateTaskParticipant(task.getId(), newAssigneeId);

    User newAssignee = finder.findUserById(newAssigneeId);

    todo.setAssignee(newAssignee);
    todoRepository.save(todo);
  }

  private void validateDueDate(LocalDate dueDate, Task task) {
    if (dueDate.isAfter(task.getDueDate())) {
      throw new BusinessException(TodoResponse.NEED_BEFORE_TASK_DUE_DATE);
    }
    if (dueDate.isBefore(LocalDate.now())) {
      throw new BusinessException(TodoResponse.NEED_AFTER_NOW_DATE);
    }
  }

  private void validateAssignee(Todo todo, Long userId) {
    if (todo.getAssignee() == null || !todo.getAssignee().getId().equals(userId)) {
      throw new BusinessException(TodoResponse.ONLY_ASSIGNEE_CAN_COMPLETE);
    }
  }

  private void validateManagerAuthority(Task task, Long userId) {
    if (!taskAuthorityUtil.isTaskManager(task, userId)) {
      throw new BusinessException(TodoResponse.ONLY_MANAGER_AUTHORIZED);
    }
  }
}
