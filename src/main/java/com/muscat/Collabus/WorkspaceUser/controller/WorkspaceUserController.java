package com.muscat.Collabus.WorkspaceUser.controller;

import com.muscat.Collabus.WorkspaceUser.model.WorkspaceUserResponseDto;
import com.muscat.Collabus.WorkspaceUser.service.WorkspaceUserService;
import com.muscat.Collabus.common.dto.ResponseDto;
import com.muscat.Collabus.config.security.CustomUserDetails;
import com.muscat.Collabus.enums.response.CommonResponse;
import com.muscat.Collabus.enums.role.WorkspaceRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workspaces")
@Tag(name = "Workspace User API", description = "워크스페이스 유저 관리 API")
public class WorkspaceUserController {

  private final WorkspaceUserService workspaceUserService;

  @GetMapping("/{workspaceId}/users")
  @Operation(
      summary = "워크스페이스 멤버 목록 조회",
      description = "특정 워크스페이스에 속한 모든 멤버를 조회합니다. 토큰 기반 인증 필수.",
      responses = {
          @ApiResponse(responseCode = "200", description = "조회 성공",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "403", description = "워크스페이스 접근 권한 없음",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  public ResponseEntity<ResponseDto> getUsersInWorkspace(
      @PathVariable Long workspaceId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    List<WorkspaceUserResponseDto> users =
        workspaceUserService.getUsersInWorkspace(workspaceId, userDetails.getUserId());
    return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS, users));
  }

  @GetMapping("/me")
  @Operation(
      summary = "내가 참여 중인 워크스페이스 목록 조회",
      description = "로그인한 유저가 현재 속한 모든 워크스페이스 정보를 반환합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "조회 성공",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  public ResponseEntity<ResponseDto> getMyWorkspaces(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    List<WorkspaceUserResponseDto> workspaces =
        workspaceUserService.getMyJoinedWorkspaces(userDetails.getUserId());
    return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS, workspaces));
  }

  @PutMapping("/{workspaceId}/users/{targetUserId}/role")
  @Operation(
      summary = "워크스페이스 내 사용자 역할 변경",
      description = "Workspace MASTER만 권한을 가지고 있으며, 본인은 변경 불가합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "변경 성공",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "403", description = "권한 없음 또는 자기 자신 변경 시도",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "404", description = "대상 유저 없음",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  public ResponseEntity<ResponseDto> updateRole(
      @PathVariable Long workspaceId,
      @PathVariable Long targetUserId,
      @RequestParam WorkspaceRole newRole,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    workspaceUserService.updateUserRole(workspaceId, targetUserId, newRole, userDetails.getUserId());
    return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS));
  }

  @DeleteMapping("/{workspaceId}/users/{userId}")
  @Operation(
      summary = "워크스페이스에서 사용자 제거",
      description = "Workspace MASTER만 실행 가능. 자기 자신은 제거할 수 없습니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "제거 성공",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "403", description = "권한 없음 또는 자기 자신 제거 시도",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "404", description = "대상 유저 없음",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  public ResponseEntity<ResponseDto> removeUser(
      @PathVariable Long workspaceId,
      @PathVariable Long userId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    workspaceUserService.removeUser(workspaceId, userId, userDetails.getUserId());
    return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS));
  }

  @DeleteMapping("/{workspaceId}/leave")
  @Operation(
      summary = "워크스페이스 나가기",
      description = """
          본인이 속한 워크스페이스에서 나갑니다.
          - 나 혼자 남은 상태라면 워크스페이스는 삭제됩니다.
          - MASTER일 경우 다음 유저에게 MASTER 권한이 위임됩니다.
          """,
      responses = {
          @ApiResponse(responseCode = "200", description = "나가기 성공",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "404", description = "유저 또는 워크스페이스 없음",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  public ResponseEntity<ResponseDto> leaveWorkspace(
      @PathVariable Long workspaceId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    workspaceUserService.leaveWorkspace(workspaceId, userDetails.getUserId());
    return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS));
  }
}
