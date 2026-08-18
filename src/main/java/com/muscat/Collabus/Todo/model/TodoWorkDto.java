package com.muscat.Collabus.Todo.model;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoWorkDto {

  private Long id;
  @NotBlank(message = "작업 제목은 필수입니다.")
  @Size(max = 255, message = "작업 제목은 255자 이하여야 합니다.")
  private String title;
  @Size(max = 10000, message = "작업 내용은 10000자 이하여야 합니다.")
  private String content;
  private Long authorId;
  private String authorDisplayName;
  private LocalDateTime updatedAt;
}
