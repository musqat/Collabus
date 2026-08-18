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
public class TodoFileDto {

  private Long id;

  @Schema(description = "다운로드 API 경로. 서버 저장 경로는 노출하지 않는다.",
      example = "/api/todo/files/1/download")
  private String downloadUrl;

  private String originalFileName;

  private Long uploaderId;

  private String uploaderDisplayName;

  private LocalDateTime uploadedAt;
}
