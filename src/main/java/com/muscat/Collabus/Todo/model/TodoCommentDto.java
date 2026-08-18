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
public class TodoCommentDto {
  private Long id;
  @NotBlank(message = "댓글 내용은 필수입니다.")
  @Size(max = 2000, message = "댓글은 2000자 이하여야 합니다.")
  private String content;
  private Long authorId;
  private String authorDisplayName;
  private LocalDateTime updatedAt;
}
