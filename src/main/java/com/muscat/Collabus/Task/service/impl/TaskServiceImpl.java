package com.muscat.Collabus.Task.service.impl;

import java.util.Set;
import com.muscat.Collabus.common.dto.PageResponseDto;
import org.springframework.data.domain.Pageable;
import com.muscat.Collabus.Notification.service.NotificationService;
import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Task.entity.TaskUser;
import com.muscat.Collabus.Task.entity.TaskUserPk;
import com.muscat.Collabus.Task.mapper.TaskMapper;
import com.muscat.Collabus.Task.mapper.TaskUserMapper;
import com.muscat.Collabus.Task.model.TaskRequestDto;
import com.muscat.Collabus.Task.model.TaskResponseDto;
import com.muscat.Collabus.Task.model.TaskUpdateRequestDto;
import com.muscat.Collabus.Task.model.TaskUserResponseDto;
import com.muscat.Collabus.Task.repository.TaskRepository;
import com.muscat.Collabus.common.util.SortGuard;
import com.muscat.Collabus.common.util.TaskSpecifications;
import com.muscat.Collabus.enums.status.TodoStatus;
import com.muscat.Collabus.common.util.TodoSpecifications;
import com.muscat.Collabus.Todo.entity.Todo;
import com.muscat.Collabus.Todo.repository.TodoRepository;
import com.muscat.Collabus.Task.model.TodoProgressDto;
import com.muscat.Collabus.Todo.event.FilesDeletedEvent;
import com.muscat.Collabus.Todo.repository.TodoFileRepository;
import com.muscat.Collabus.Task.repository.TaskUserRepository;
import com.muscat.Collabus.Task.service.TaskService;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.Workspace.entity.Workspace;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.common.exception.ResourceNotFoundException;
import com.muscat.Collabus.common.util.EntityFinderUtil;
import com.muscat.Collabus.common.util.TaskAuthorityUtil;
import com.muscat.Collabus.enums.NotificationType;
import com.muscat.Collabus.enums.response.CommonResponse;
import com.muscat.Collabus.enums.response.TaskResponse;
import com.muscat.Collabus.enums.role.TaskRole;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private final SortGuard sortGuard;

    // 담당자 이름순 정렬을 위해 연관 경로를 하나 열어 둔다
    private static final Set<String> SORT_PATHS = Set.of("taskManager.displayName");

    private final TaskRepository taskRepository;
    private final TodoFileRepository todoFileRepository;
    private final TodoRepository todoRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TaskUserRepository taskUserRepository;
    private final TaskMapper taskMapper;
    private final TaskUserMapper taskUserMapper;
    private final TaskAuthorityUtil taskAuthorityUtil;
    private final EntityFinderUtil finder;
    private final NotificationService notificationService;

    @Transactional
    @Override
    public TaskResponseDto createTask(TaskRequestDto dto, Long userId) {
        User creator = finder.findUserById(userId);
        Workspace workspace = finder.findWorkspaceById(dto.getWorkspaceId());

        // MASTER 또는 MANAGER만 Task 생성 가능
        taskAuthorityUtil.validateCanCreateTask(workspace, userId);

        // Task Manager 처리 - managerId가 null이면 생성자를 Manager로 지정
        User taskManager = null;
        if (dto.getManagerId() != null) {
            taskManager = finder.findUserById(dto.getManagerId());
        } else {
            taskManager = creator; // managerId가 없으면 생성자가 Manager
        }

        Task task = taskRepository.save(taskMapper.mapToEntity(dto, workspace, taskManager));

        // Task Manager를 TaskUser에 추가
        if (taskManager != null) {
            taskUserRepository.save(new TaskUser(
                    new TaskUserPk(task.getId(), taskManager.getId()), task, taskManager, TaskRole.MANAGER));
        }

        // 추가 멤버들을 TaskUser에 추가
        if (dto.getMemberIds() != null && !dto.getMemberIds().isEmpty()) {
            for (Long memberId : dto.getMemberIds()) {
                // 이미 매니저로 추가된 경우 스킵
                if (taskManager != null && memberId.equals(taskManager.getId())) {
                    continue;
                }
                User member = finder.findUserById(memberId);
                taskUserRepository.save(new TaskUser(
                        new TaskUserPk(task.getId(), memberId), task, member, TaskRole.NORMAL));

                // 멤버에게 Task 할당 알림 전송
                if (!memberId.equals(userId)) {
                    String message = String.format("'%s' 태스크에 할당되었습니다.", task.getTitle());
                    notificationService.createNotification(memberId,
                            NotificationType.TASK_ASSIGNED, message, task.getId());
                }
            }
        }

        // Task Manager에게도 알림 전송 (생성자가 아닌 경우)
        if (taskManager != null && !taskManager.getId().equals(userId)) {
            String message = String.format("'%s' 태스크의 매니저로 지정되었습니다.", task.getTitle());
            notificationService.createNotification(taskManager.getId(),
                    NotificationType.TASK_ASSIGNED, message, task.getId());
        }

        return taskMapper.mapToDto(task);
    }

    @Override
    public TaskResponseDto getTask(Long taskId, Long requesterId) {
        Task task = finder.findTaskById(taskId);
        taskAuthorityUtil.validateCanViewTask(task, requesterId);
        return taskMapper.mapToDto(task);
    }

    @Override
    public TodoProgressDto getWorkspaceProgress(Long workspaceId, Long requesterId) {
        taskAuthorityUtil.validateWorkspaceMember(workspaceId, requesterId);

        Specification<Todo> visible = TodoSpecifications.inWorkspace(workspaceId);
        if (!taskAuthorityUtil.canViewAllTasks(workspaceId, requesterId)) {
            visible = visible.and(TodoSpecifications.inTaskParticipatedBy(requesterId));
        }

        return countProgress(visible);
    }

    private TodoProgressDto countProgress(Specification<Todo> scope) {
        return TodoProgressDto.builder()
                .total(todoRepository.count(scope))
                .inProgress(countByStatus(scope, TodoStatus.IN_PROGRESS))
                .waitingReview(countByStatus(scope, TodoStatus.WAITING_REVIEW))
                .confirmed(countByStatus(scope, TodoStatus.CONFIRMED))
                .build();
    }

    private long countByStatus(Specification<Todo> scope, TodoStatus status) {
        return todoRepository.count(scope.and(TodoSpecifications.hasStatus(status)));
    }

    @Transactional
    @Override
    public TaskResponseDto updateTask(Long taskId, TaskUpdateRequestDto dto, Long userId) {
        Task task = finder.findTaskById(taskId);
        taskAuthorityUtil.validateCanManageTask(task, userId);

        task.update(dto.getTitle(), dto.getDescription(), dto.getDueDate());
        return taskMapper.mapToDto(task);
    }

    @Transactional
    @Override
    public void deleteTask(Long taskId, Long userId) {
        Task task = finder.findTaskById(taskId);
        // Workspace Master 또는 Task Manager 만 삭제할 수 있다
        taskAuthorityUtil.validateCanManageTask(task, userId);

        // 삭제 전에 하위 첨부 파일 경로를 모아 이벤트로 넘긴다
        List<String> fileUrls = todoFileRepository.findAllByWork_Todo_Task_Id(taskId)
                .stream().map(TodoFileRepository.FileLocation::getFileUrl).toList();

        taskRepository.delete(task);
        eventPublisher.publishEvent(new FilesDeletedEvent(fileUrls));
    }

    @Override
    public PageResponseDto<TaskResponseDto> getTasksByWorkspace(Long workspaceId, Long requesterId,
                                                                String keyword, Pageable pageable) {
        taskAuthorityUtil.validateWorkspaceMember(workspaceId, requesterId);

        Specification<Task> spec = TaskSpecifications.inWorkspace(workspaceId);

        if (!taskAuthorityUtil.canViewAllTasks(workspaceId, requesterId)) {
            spec = spec.and(TaskSpecifications.participatedBy(requesterId));
        }

        // 검색어가 있으면 조건을 더한다
        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and(TaskSpecifications.matches(keyword));
        }

        return PageResponseDto.of(
                taskRepository.findAll(spec, sortGuard.apply(pageable, Task.class, SORT_PATHS)),
                taskMapper::mapToDto);
    }

    @Transactional
    @Override
    public void assignUserToTask(Long taskId, Long targetUserId, Long requesterId) {
        Task task = finder.findTaskById(taskId);
        taskAuthorityUtil.validateWorkspaceMaster(task, requesterId);

        User targetUser = finder.findUserById(targetUserId);
        if (taskUserRepository.existsByTaskAndUser(task, targetUser)) {
            throw new BusinessException(TaskResponse.TASK_USER_ALREADY_EXISTS);
        }

        taskUserRepository.save(new TaskUser(
                new TaskUserPk(taskId, targetUserId), task, targetUser, TaskRole.NORMAL));

        // 할당된 사용자에게 알림 전송
        if (!targetUserId.equals(requesterId)) {
            String message = String.format("'%s' 태스크에 할당되었습니다.", task.getTitle());
            notificationService.createNotification(targetUserId,
                    NotificationType.TASK_ASSIGNED, message, taskId);
        }
    }

    @Transactional
    @Override
    public void removeUserFromTask(Long taskId, Long targetUserId, Long requesterId) {
        Task task = finder.findTaskById(taskId);
        taskAuthorityUtil.validateCanManageTask(task, requesterId);

        if (taskAuthorityUtil.isTaskManager(task, requesterId) && requesterId.equals(targetUserId)) {
            throw new BusinessException(CommonResponse.CANNOT_REMOVE_SELF);
        }

        TaskUser taskUser = taskUserRepository.findByTaskAndUser(task,
                        finder.findUserById(targetUserId))
                .orElseThrow(
                        () -> new ResourceNotFoundException(TaskResponse.TASK_USER_NOT_FOUND));
        taskUserRepository.delete(taskUser);
    }

    @Transactional
    @Override
    public void assignTaskManager(Long taskId, Long newManagerId, Long requesterId) {
        Task task = finder.findTaskById(taskId);
        taskAuthorityUtil.validateWorkspaceMaster(task, requesterId);

        User newManager = finder.findUserById(newManagerId);
        TaskUser newManagerTaskUser = taskUserRepository.findByTaskAndUser(task, newManager)
                .orElseThrow(
                        () -> new ResourceNotFoundException(TaskResponse.TASK_USER_NOT_FOUND));

        taskUserRepository.findAllByTask(task).stream()
                .filter(tu -> tu.getRole() == TaskRole.MANAGER)
                .findFirst()
                .ifPresent(current -> {
                    current.changeRole(TaskRole.NORMAL);
                    taskUserRepository.save(current);
                });

        newManagerTaskUser.changeRole(TaskRole.MANAGER);
        task.changeManager(newManager);
        taskRepository.save(task);
        taskUserRepository.save(newManagerTaskUser);
    }

    @Override
    public PageResponseDto<TaskUserResponseDto> getTaskMembers(Long taskId, Long requesterId,
                                                               Pageable pageable) {
        taskAuthorityUtil.validateCanViewTask(finder.findTaskById(taskId), requesterId);
        return PageResponseDto.of(
                taskUserRepository.findAllByTask_Id(taskId, sortGuard.apply(pageable, TaskUser.class)),
                taskUserMapper::mapToDto);
    }

    @Override
    public TodoProgressDto getTaskProgress(Long taskId, Long requesterId) {
        taskAuthorityUtil.validateCanViewTask(finder.findTaskById(taskId), requesterId);
        return countProgress(TodoSpecifications.inTask(taskId));
    }
}
