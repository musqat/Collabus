package com.muscat.Collabus.Todo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoFileDto {
  private Long id;
  private String fileUrl;
  private String originalName;
}
