package com.muscat.Collabus.Todo.mapper;

import com.muscat.Collabus.Todo.entity.TodoWork;
import com.muscat.Collabus.Todo.model.TodoWorkDto;
import org.springframework.stereotype.Component;

@Component
public class TodoWorkMapper {

  public TodoWorkDto mapToDto(TodoWork todoWork) {
    return TodoWorkDto.builder()
        .id(todoWork.getId())
        .title(todoWork.getTitle())
        .content(todoWork.getContent())
        .authorId(todoWork.getAuthor().getId())
        .authorDisplayName(todoWork.getAuthor().getDisplayName())
        .updatedAt(todoWork.getUpdatedAt())
        .build();
  }
}
