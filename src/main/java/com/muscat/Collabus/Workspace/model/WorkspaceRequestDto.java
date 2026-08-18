package com.muscat.Collabus.Workspace.model;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
  @NotBlank(message = "워크스페이스 이름은 필수입니다.")
  @Size(max = 255, message = "워크스페이스 이름은 255자 이하여야 합니다.")
  private String workspaceName;

  @Schema(description = "워크스페이스 설명")
  @NotBlank(message = "워크스페이스 설명은 필수입니다.")
  @Size(max = 500, message = "설명은 500자 이하여야 합니다.")
  private String description;
}
