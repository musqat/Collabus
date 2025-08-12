package com.muscat.Collabus.Todo.service;

import com.muscat.Collabus.Todo.model.TodoRequestDto;
import com.muscat.Collabus.Todo.model.TodoResponseDto;
import java.util.List;

public interface TodoService {

  // 생성
  TodoResponseDto createTodo(TodoRequestDto dto, Long creatorId);

  // 수정
  TodoResponseDto updateTodo(Long todoId, TodoRequestDto dto, Long updaterId);

  // 삭제
  void deleteTodo(Long todoId, Long userId);

  // 조회
  TodoResponseDto getTodoById(Long todoId);

  List<TodoResponseDto> getTodosByTask(Long taskId, String status); // status 필터링 추가

  // 상태 변경
  void completeOwnTodo(Long todoId, Long userId);         // 나의 할일 완료 처리

  TodoResponseDto confirmTodoCompletion(Long todoId, Long taskManagerId); // TM이 확인

  // 담당자 변경 ( TM -> Member )
  void changeAssignee(Long todoId, Long newAssigneeId, Long managerId);
}
