package com.muscat.Collabus.Todo.service.impl;


import com.muscat.Collabus.common.util.TaskAuthorityUtil;
import com.muscat.Collabus.common.util.SortGuard;
import com.muscat.Collabus.common.dto.PageResponseDto;
import org.springframework.data.domain.Pageable;
import com.muscat.Collabus.Todo.entity.Todo;
import com.muscat.Collabus.Todo.entity.TodoWork;
import com.muscat.Collabus.Todo.mapper.TodoWorkMapper;
import com.muscat.Collabus.Todo.model.TodoWorkDto;
import com.muscat.Collabus.Todo.repository.TodoWorkRepository;
import com.muscat.Collabus.Todo.service.TodoWorkService;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.common.util.EntityFinderUtil;
import com.muscat.Collabus.enums.response.TodoResponse;
import com.muscat.Collabus.enums.status.TodoStatus;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoWorkServiceImpl implements TodoWorkService {

    private final TaskAuthorityUtil taskAuthorityUtil;
    private final SortGuard sortGuard;
    private final TodoWorkMapper todoWorkMapper;
    private final TodoWorkRepository todoWorkRepository;
    private final EntityFinderUtil finder;

    @Override
    @Transactional
    public TodoWorkDto createWork(Long todoId, TodoWorkDto dto, Long userId) {
        Todo todo = finder.findTodoById(todoId);

        // 참여자 확인
        taskAuthorityUtil.validateCanViewTask(todo.getTask(), userId);

        // 확정된 Todo에는 작업 내용 작성 불가
        if (todo.getStatus() == TodoStatus.CONFIRMED) {
            throw new BusinessException(TodoResponse.CANNOT_WORK_IN_CONFIRMED);
        }

        // WAITING_REVIEW 상태면 다시 IN_PROGRESS로 변경
        if (todo.getStatus() == TodoStatus.WAITING_REVIEW) {
            todo.reopenIfWaitingReview();
        }

        User user = finder.findUserById(userId);
        TodoWork work = TodoWork.builder()
                .todo(todo)
                .title(dto.getTitle())
                .content(dto.getContent())
                .author(user)
                .build();

        return todoWorkMapper.mapToDto(todoWorkRepository.save(work));
    }

    @Override
    @Transactional
    public TodoWorkDto updateWork(Long workId, TodoWorkDto dto, Long userId) {
        TodoWork work = todoWorkRepository.findById(workId)
                .orElseThrow(() -> new BusinessException(TodoResponse.TODO_WORK_NOT_FOUND));

        if (!work.getAuthor().getId().equals(userId)) {
            throw new BusinessException(TodoResponse.UNAUTHORIZED_TODO_WORK);
        }

        work.update(dto.getTitle(), dto.getContent());

        return todoWorkMapper.mapToDto(work);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<TodoWorkDto> getWorksByTodoId(Long todoId, Long requesterId,
                                                         Pageable pageable) {
        taskAuthorityUtil.requireViewableTodo(todoId, requesterId);

        return PageResponseDto.of(
                todoWorkRepository.findAllByTodoId(todoId, sortGuard.apply(pageable, TodoWork.class)),
                todoWorkMapper::mapToDto);
    }

    @Override
    @Transactional
    public void deleteWork(Long workId, Long userId) {
        TodoWork work = todoWorkRepository.findById(workId)
                .orElseThrow(() -> new BusinessException(TodoResponse.TODO_WORK_NOT_FOUND));

        if (!work.getAuthor().getId().equals(userId)) {
            throw new BusinessException(TodoResponse.UNAUTHORIZED_TODO_WORK);
        }

        todoWorkRepository.delete(work);
    }
}
