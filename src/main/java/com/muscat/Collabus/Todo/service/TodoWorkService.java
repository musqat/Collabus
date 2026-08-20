package com.muscat.Collabus.Todo.service;

import com.muscat.Collabus.common.dto.PageResponseDto;
import org.springframework.data.domain.Pageable;
import com.muscat.Collabus.Todo.model.TodoWorkDto;

public interface TodoWorkService {
    // 작업 내용을 작성한다. 볼 수 있는 사람이면 쓸 수 있다
    TodoWorkDto createWork(Long todoId, TodoWorkDto dto, Long userId);

    // 작업 내용을 수정한다
    TodoWorkDto updateWork(Long workId, TodoWorkDto dto, Long userId);

    // Todo 의 작업 내용 목록
    PageResponseDto<TodoWorkDto> getWorksByTodoId(Long todoId, Long requesterId,
        Pageable pageable);

    // 작업 내용을 삭제한다
    void deleteWork(Long workId, Long userId);

}
