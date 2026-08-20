package com.muscat.Collabus.Todo.service;

import com.muscat.Collabus.Todo.model.TodoRequestDto;
import com.muscat.Collabus.Todo.model.TodoUpdateRequestDto;
import com.muscat.Collabus.Todo.model.TodoResponseDto;
import com.muscat.Collabus.common.dto.PageResponseDto;
import org.springframework.data.domain.Pageable;

public interface TodoService {

  // Todo 를 만든다
  TodoResponseDto createTodo(TodoRequestDto dto, Long creatorId);

  // Todo 를 수정한다
  TodoResponseDto updateTodo(Long todoId, TodoUpdateRequestDto dto, Long updaterId);

  // Todo 를 삭제한다. 하위 작업 내용과 첨부 파일까지 지운다
  void deleteTodo(Long todoId, Long userId);

  // Todo 단건 조회
  TodoResponseDto getTodoById(Long todoId, Long requesterId);

  // Task 의 Todo 목록. status 를 주면 그 상태만 걸러낸다
  PageResponseDto<TodoResponseDto> getTodosByTask(Long taskId, Long requesterId, String status,
      Pageable pageable);

  // 완료를 요청한다. 상태가 WAITING_REVIEW 로 바뀐다
  void completeOwnTodo(Long todoId, Long userId);

  // 완료를 승인한다. 상태가 CONFIRMED 로 바뀐다
  TodoResponseDto confirmTodoCompletion(Long todoId, Long taskManagerId);

  // 담당자를 바꾼다
  void changeAssignee(Long todoId, Long newAssigneeId, Long managerId);
}
