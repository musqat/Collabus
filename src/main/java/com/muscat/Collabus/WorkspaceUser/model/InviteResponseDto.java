package com.muscat.Collabus.WorkspaceUser.model;

import com.muscat.Collabus.enums.role.WorkspaceRole;
import com.muscat.Collabus.enums.status.InviteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "Invite", description = "워크스페이스 초대 응답 DTO")
public class InviteResponseDto {

  @Schema(description = "초대 ID")
  private Long inviteId;

  @Schema(description = "워크스페이스 ID")
  private Long workspaceId;

  @Schema(description = "워크스페이스 이름")
  private String workspaceName;

  @Schema(description = "초대한 사람의 닉네임")
  private String inviterDisplayName;

  @Schema(description = "초대받은 역할")
  private WorkspaceRole role;

  @Schema(description = "초대 상태 (PENDING, ACCEPTED, REJECTED)")
  private InviteStatus status;
}
