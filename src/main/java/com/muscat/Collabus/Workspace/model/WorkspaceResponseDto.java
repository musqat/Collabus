package com.muscat.Collabus.Workspace.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "Workspace", description = "워크스페이스 응답 DTO")
public class WorkspaceResponseDto {

  @Schema(description = "워크스페이스 ID")
  private Long id;

  @Schema(description = "워크스페이스 이름")
  private String workspaceName;

  @Schema(description = "워크스페이스 설명")
  private String description;

  @Schema(description = "워크스페이스 생성자 닉네임")
  private String founderDisplayName;
}
