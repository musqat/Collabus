package com.muscat.Collabus.Todo.service;

import com.muscat.Collabus.Todo.model.TodoWorkDto;
import java.util.List;

public interface TodoWorkService {
  TodoWorkDto createWork(Long todoId, TodoWorkDto dto, Long userId);

  TodoWorkDto updateWork(Long workId, TodoWorkDto dto, Long userId);

  List<TodoWorkDto> getWorksByTodoId(Long todoId);

  void deleteWork(Long workId, Long userId);

}
