package com.muscat.Collabus.Todo.service;

import com.muscat.Collabus.common.dto.PageResponseDto;
import org.springframework.data.domain.Pageable;
import com.muscat.Collabus.Todo.model.TodoWorkDto;

public interface TodoWorkService {
    TodoWorkDto createWork(Long todoId, TodoWorkDto dto, Long userId);

    TodoWorkDto updateWork(Long workId, TodoWorkDto dto, Long userId);

    PageResponseDto<TodoWorkDto> getWorksByTodoId(Long todoId, Pageable pageable);

    void deleteWork(Long workId, Long userId);

}
