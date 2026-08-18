package com.muscat.Collabus.Todo.service.impl;

import com.muscat.Collabus.Notification.service.NotificationService;
import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Task.repository.TaskUserRepository;
import com.muscat.Collabus.Todo.entity.Todo;
import com.muscat.Collabus.Todo.mapper.TodoMapper;
import com.muscat.Collabus.Todo.model.TodoRequestDto;
import com.muscat.Collabus.Todo.model.TodoResponseDto;
import com.muscat.Collabus.Todo.repository.TodoCommentRepository;
import com.muscat.Collabus.Todo.repository.TodoFileRepository;
import com.muscat.Collabus.Todo.repository.TodoRepository;
import com.muscat.Collabus.Todo.repository.TodoWorkRepository;
import com.muscat.Collabus.Todo.service.TodoService;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.common.dto.PageResponseDto;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.common.util.EntityFinderUtil;
import com.muscat.Collabus.common.util.ParticipantUtil;
import com.muscat.Collabus.common.util.TaskAuthorityUtil;
import com.muscat.Collabus.enums.NotificationType;
import com.muscat.Collabus.enums.response.TodoResponse;
import com.muscat.Collabus.enums.role.TaskRole;
import com.muscat.Collabus.enums.status.TodoStatus;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final TodoWorkRepository todoWorkRepository;
    private final TodoCommentRepository todoCommentRepository;
    private final TodoFileRepository todoFileRepository;
    private final TodoMapper todoMapper;
    private final ParticipantUtil participantUtil;
    private final TaskAuthorityUtil taskAuthorityUtil;
    private final EntityFinderUtil finder;
    private final NotificationService notificationService;
    private final TaskUserRepository taskUserRepository;

    @Override
    @Transactional
    public TodoResponseDto createTodo(TodoRequestDto dto, Long creatorId) {
        Task task = finder.findTaskById(dto.getTaskId());
        // Task Manager 또는 Workspace Master/Manager만 Todo 생성 가능
        taskAuthorityUtil.validateCanManageTask(task, creatorId);

        validateDueDate(dto.getDueDate(), task);

        // 담당자 미지정 시 생성자를 담당자로 지정
        Long assigneeId = dto.getAssigneeId() != null ? dto.getAssigneeId() : creatorId;
        User assignee = finder.findUserById(assigneeId);
        Todo todo = todoMapper.mapToEntity(dto, task, assignee);
        Todo savedTodo = todoRepository.save(todo);

        // 담당자가 생성자와 다른 경우 알림 전송
        if (!assigneeId.equals(creatorId)) {
            String message = String.format("'%s' 할일이 새로 할당되었습니다.", todo.getTitle());
            notificationService.createNotification(assigneeId,
                    NotificationType.TASK_ASSIGNED, message, savedTodo.getId());
        }

        return todoMapper.mapToDto(savedTodo);
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

        todoFileRepository.deleteAllByTodoId(todoId);
        todoWorkRepository.deleteAllByTodoId(todoId);
        todoCommentRepository.deleteAllByTodoId(todoId);
        todoRepository.delete(todo);
    }

    @Override
    public TodoResponseDto getTodoById(Long todoId) {
        return todoMapper.mapToDto(finder.findTodoById(todoId));
    }

    @Override
    public PageResponseDto<TodoResponseDto> getTodosByTask(Long taskId, String status,
            Pageable pageable) {
        Page<Todo> todos = (status != null)
                ? todoRepository.findAllByTaskIdAndStatus(taskId, parseStatus(status), pageable)
                : todoRepository.findAllByTaskId(taskId, pageable);

        return PageResponseDto.of(todos, todoMapper::mapToDto);
    }

    private TodoStatus parseStatus(String status) {
        try {
            return TodoStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            // 잘못된 쿼리 파라미터는 500이 아니라 400으로 응답해야 한다
            throw new BusinessException(TodoResponse.INVALID_STATUS);
        }
    }

    @Override
    @Transactional
    public void completeOwnTodo(Long todoId, Long userId) {
        Todo todo = finder.findTodoById(todoId);
        participantUtil.validateTaskParticipant(todo.getTask().getId(), userId);
        validateAssignee(todo, userId);

        todo.setDoneAt(LocalDateTime.now());
        todo.setStatus(TodoStatus.WAITING_REVIEW);
        todoRepository.save(todo);

        // Task 모든 MANAGER에게 검수 요청 알림 전송
        Task task = todo.getTask();
        String reviewMessage = String.format("'%s' 할일이 완료되어 검수를 기다리고 있습니다.", todo.getTitle());
        taskUserRepository.findByTaskAndRole(task, TaskRole.MANAGER).stream()
                .filter(m -> !m.getUser().getId().equals(userId))
                .forEach(m -> notificationService.createNotification(
                        m.getUser().getId(), NotificationType.TODO_REVIEW_REQUESTED, reviewMessage, todoId));
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
        Todo savedTodo = todoRepository.save(todo);

        // 담당자에게 검수 완료 알림 전송
        if (todo.getAssignee() != null && !todo.getAssignee().getId().equals(taskManagerId)) {
            String message = String.format("'%s' 할일이 검수 완료되었습니다.", todo.getTitle());
            notificationService.createNotification(todo.getAssignee().getId(),
                    NotificationType.TODO_COMPLETED, message, todoId);
        }

        return todoMapper.mapToDto(savedTodo);
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

        // 새로운 담당자에게 할당 알림 전송
        if (!newAssigneeId.equals(managerId)) {
            String message = String.format("'%s' 할일이 새로 할당되었습니다.", todo.getTitle());
            notificationService.createNotification(newAssigneeId,
                    NotificationType.TASK_ASSIGNED, message, todoId);
        }
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
        // Workspace MASTER 또는 Task MANAGER만 가능
        if (!taskAuthorityUtil.canManageTask(task, userId)) {
            throw new BusinessException(TodoResponse.ONLY_MANAGER_AUTHORIZED);
        }
    }
}
