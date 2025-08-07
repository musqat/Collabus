package com.muscat.Collabus.WorkspaceUser.model;

import com.muscat.Collabus.enums.role.WorkspaceRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "WorkspaceUserRequest", description = "워크스페이스에 사용자 요청 DTO")
public class WorkspaceUserRequestDto {

  @Schema(description = "유저 ID", example = "5")
  private Long userId;

  @Schema(description = "워크스페이스 내 역할", example = "MEMBER")
  private WorkspaceRole role;
}
