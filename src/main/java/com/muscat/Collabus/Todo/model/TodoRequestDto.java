package com.muscat.Collabus.Todo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "할 일 생성/수정 요청 DTO")
public class TodoRequestDto {

  @Schema(description = "할 일이 속한 태스크 ID", example = "3", required = true)
  @NotNull(message = "태스크 ID는 필수입니다.")
  private Long taskId;

  @Schema(description = "할 일 제목", example = "UI 디자인")
  @NotBlank(message = "할 일 제목은 필수입니다.")
  @Size(max = 255, message = "할 일 제목은 255자 이하여야 합니다.")
  private String title;

  @Schema(description = "할 일 설명", example = "메인 페이지 UI 시안 작업")
  @Size(max = 2000, message = "설명은 2000자 이하여야 합니다.")
  private String description;

  @Schema(description = "마감일", example = "2025-08-10", required = true)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  @NotNull(message = "마감일은 필수입니다.")
  private LocalDate dueDate;

  @Schema(description = "담당자 ID (선택, 없으면 TM이 담당자)", example = "5")
  private Long assigneeId;
}
