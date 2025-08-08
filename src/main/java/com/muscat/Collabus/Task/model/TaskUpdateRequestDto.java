package com.muscat.Collabus.Task.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "태스크 수정 DTO")
public class TaskUpdateRequestDto {
  private String title;
  private String description;
  private LocalDate dueDate;
}
