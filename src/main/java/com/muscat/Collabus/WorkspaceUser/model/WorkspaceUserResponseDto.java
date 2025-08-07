package com.muscat.Collabus.WorkspaceUser.model;

import com.muscat.Collabus.enums.role.WorkspaceRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "WorkspaceUserResponse", description = "워크스페이스 유저 응답 DTO")
public class WorkspaceUserResponseDto {

  @Schema(description = "유저 ID", example = "5")
  private Long userId;

  @Schema(description = "워크스페이스 ID", example = "3")
  private Long workspaceId;

  @Schema(description = "유저 닉네임", example = "musqat#1234")
  private String displayName;

  @Schema(description = "워크스페이스 내 역할", example = "MASTER")
  private WorkspaceRole role;
}
