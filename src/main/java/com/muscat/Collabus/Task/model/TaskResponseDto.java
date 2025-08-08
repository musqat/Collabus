package com.muscat.Collabus.Task.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@Schema(description = "태스크 응답 DTO")
public class TaskResponseDto {

  @Schema(description = "태스크 ID", example = "1")
  private Long id;

  @Schema(description = "태스크 제목", example = "UI 개선 작업")
  private String title;

  @Schema(description = "태스크 설명", example = "마이페이지 디자인 개선")
  private String description;

  @Schema(description = "마감일", example = "2025-08-10")
  private LocalDate dueDate;

  @Schema(description = "매니저 닉네임", example = "muscat#1234")
  private String managerDisplayName;

  @Schema(description = "워크스페이스 ID", example = "1")
  private Long workspaceId;
}
