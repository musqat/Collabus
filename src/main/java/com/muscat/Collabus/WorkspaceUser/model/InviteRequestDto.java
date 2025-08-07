package com.muscat.Collabus.WorkspaceUser.model;

import com.muscat.Collabus.enums.role.WorkspaceRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "InviteRequest", description = "워크스페이스 초대 요청 DTO")
public class InviteRequestDto {

  @Schema(description = "초대받을 유저의 ID", example = "5")
  private Long userId;

  @Schema(description = "초대받을 역할", example = "NORMAL")
  private WorkspaceRole role;
}
