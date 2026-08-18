package com.muscat.Collabus.Todo.service;

import com.muscat.Collabus.Todo.model.TodoRequestDto;
import com.muscat.Collabus.Todo.model.TodoResponseDto;
import com.muscat.Collabus.common.dto.PageResponseDto;
import org.springframework.data.domain.Pageable;

public interface TodoService {

  // 생성
  TodoResponseDto createTodo(TodoRequestDto dto, Long creatorId);

  // 수정
  TodoResponseDto updateTodo(Long todoId, TodoRequestDto dto, Long updaterId);

  // 삭제
  void deleteTodo(Long todoId, Long userId);

  // 조회
  TodoResponseDto getTodoById(Long todoId);

  PageResponseDto<TodoResponseDto> getTodosByTask(Long taskId, String status, Pageable pageable);

  // 상태 변경
  void completeOwnTodo(Long todoId, Long userId);         // 나의 할일 완료 처리

  TodoResponseDto confirmTodoCompletion(Long todoId, Long taskManagerId); // TM이 확인

  // 담당자 변경 ( TM -> Member )
  void changeAssignee(Long todoId, Long newAssigneeId, Long managerId);
}
