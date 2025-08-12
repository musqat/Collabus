package com.muscat.Collabus.Todo.mapper;

import com.muscat.Collabus.Todo.entity.TodoFile;
import com.muscat.Collabus.Todo.model.TodoFileDto;
import org.springframework.stereotype.Component;

@Component
public class TodoFileMapper {

  public TodoFileDto mapToDto(TodoFile todoFile) {
    return TodoFileDto.builder()
        .id(todoFile.getId())
        .fileUrl(todoFile.getFileUrl())
        .originalName(todoFile.getOriginalName())
        .build();
  }
}
