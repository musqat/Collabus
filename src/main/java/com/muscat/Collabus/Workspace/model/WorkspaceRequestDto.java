package com.muscat.Collabus.Workspace.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "WorkspaceUpdate", description = "워크스페이스 요청 DTO")
public class WorkspaceRequestDto {

  @Schema(description = "워크스페이스 이름")
  private String workspaceName;

  @Schema(description = "워크스페이스 설명")
  private String description;
}
