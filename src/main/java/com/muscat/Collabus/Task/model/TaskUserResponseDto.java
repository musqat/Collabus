package com.muscat.Collabus.Task.model;

import com.muscat.Collabus.enums.role.TaskRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "태스크 멤버 응답 DTO")
public class TaskUserResponseDto {

  @Schema(description = "유저 ID", example = "5")
  private Long userId;

  @Schema(description = "유저 닉네임", example = "muscat#1234")
  private String displayName;

  @Schema(description = "태스크 내 역할", example = "MANAGER")
  private TaskRole role;
}
