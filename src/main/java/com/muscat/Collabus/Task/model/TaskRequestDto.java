package com.muscat.Collabus.Task.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Schema(description = "태스크 요청 DTO")
public class TaskRequestDto {

  @Schema(description = "태스크 제목", example = "UI 개선 작업")
  private String title;

  @Schema(description = "태스크 설명", example = "마이페이지 디자인 개선")
  private String description;

  @Schema(description = "마감일", example = "2025-08-10")
  private LocalDate dueDate;

  @Schema(description = "워크스페이스 ID", example = "1")
  private Long workspaceId;

  @Schema(description = "Task Manager ID (선택, null이면 미정)", example = "1")
  private Long managerId;

  @Schema(description = "Task Member IDs (선택, 추가 멤버)", example = "[2, 3]")
  private List<Long> memberIds;
}
