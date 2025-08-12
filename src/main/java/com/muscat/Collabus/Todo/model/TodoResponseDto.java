package com.muscat.Collabus.Todo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "할 일 응답 DTO")
public class TodoResponseDto {

  @Schema(description = "할 일 ID", example = "1")
  private Long id;

  @Schema(description = "제목", example = "기획안 작성")
  private String title;

  @Schema(description = "설명", example = "첫 번째 기획안 초안 작성")
  private String description;

  @Schema(description = "마감일", example = "2025-08-06T00:00:00")
  private LocalDateTime dueDate;

  @Schema(description = "상태", example = "IN_PROGRESS")
  private String status;

  @Schema(description = "완료 여부", example = "false")
  private boolean isDone;

  @Schema(description = "완료 시각", example = "2025-08-06T15:23:00", nullable = true)
  private LocalDateTime doneAt;

  @Schema(description = "할 일이 속한 태스크 ID", example = "5")
  private Long taskId;

  @Schema(description = "담당자 ID", example = "10", nullable = true)
  private Long assigneeId;

  @Schema(description = "담당자 닉네임", example = "john_doe", nullable = true)
  private String assigneeNickname;

  @Schema(description = "담당자 디스플레이 이름", example = "john_doe#1234", nullable = true)
  private String assigneeDisplayName;
}
