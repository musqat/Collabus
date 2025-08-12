package com.muscat.Collabus.Todo.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoCommentDto {
  private Long id;
  private String content;
  private Long authorId;
  private String authorDisplayName;
  private LocalDateTime updatedAt;
}
