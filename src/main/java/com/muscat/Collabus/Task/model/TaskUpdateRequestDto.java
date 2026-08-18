package com.muscat.Collabus.Task.model;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
  @NotBlank(message = "태스크 제목은 필수입니다.")
  @Size(max = 255, message = "태스크 제목은 255자 이하여야 합니다.")
  private String title;
  @Size(max = 2000, message = "설명은 2000자 이하여야 합니다.")
  private String description;
  @NotNull(message = "마감일은 필수입니다.")
  private LocalDate dueDate;
}
