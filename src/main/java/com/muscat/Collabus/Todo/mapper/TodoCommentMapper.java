package com.muscat.Collabus.Todo.mapper;

import com.muscat.Collabus.Todo.entity.TodoComment;
import com.muscat.Collabus.Todo.model.TodoCommentDto;
import org.springframework.stereotype.Component;

@Component
public class TodoCommentMapper {

  public TodoCommentDto mapToDto(TodoComment todoComment) {
    return TodoCommentDto.builder()
        .id(todoComment.getId())
        .content(todoComment.getContent())
        .createdAt(todoComment.getCreatedAt())
        .updatedAt(todoComment.getUpdatedAt())
        .authorId(todoComment.getAuthor().getId())
        .authorDisplayName(todoComment.getAuthor().getDisplayName())
        .build();
  }
}
