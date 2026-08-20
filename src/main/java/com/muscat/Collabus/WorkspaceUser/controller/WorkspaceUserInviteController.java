package com.muscat.Collabus.WorkspaceUser.controller;

import com.muscat.Collabus.WorkspaceUser.model.InviteRequestDto;
import com.muscat.Collabus.WorkspaceUser.service.WorkspaceUserInviteService;
import com.muscat.Collabus.common.dto.ResponseDto;
import com.muscat.Collabus.config.security.CustomUserDetails;
import com.muscat.Collabus.enums.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springdoc.core.annotations.ParameterObject;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workspaces")
@Tag(name = "Workspace Invite API", description = "워크스페이스 초대 관리 API")
public class WorkspaceUserInviteController {

  private final WorkspaceUserInviteService inviteService;

  @PostMapping("/{workspaceId}/invites")
  @Operation(
      summary = "워크스페이스 초대",
      description = """
          Workspace MASTER가 특정 유저를 워크스페이스에 초대합니다.
          - 초대는 userId 기준
          - 자기 자신 초대 불가
          - 이미 초대한 경우 불가
          """,
      responses = {
          @ApiResponse(responseCode = "200", description = "초대 성공",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "400", description = "자기 자신 초대 또는 이미 초대됨",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "403", description = "권한 없음 (MASTER 아님)",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "404", description = "워크스페이스 또는 유저 없음",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  public ResponseEntity<ResponseDto> inviteUser(
      @PathVariable Long workspaceId,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody @Valid InviteRequestDto requestDto
  ) {
    inviteService.inviteUserToWorkspace(userDetails.getUserId(), workspaceId, requestDto);
    return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS));
  }

  @GetMapping("/invites/me")
  @Operation(
      summary = "받은 초대 목록",
      description = "사용자가 받은 대기 중 초대 목록을 페이지 단위로 조회합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "조회 성공",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  public ResponseEntity<ResponseDto> getMyInvites(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @ParameterObject @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
  ) {
    return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
        inviteService.getMyInvites(userDetails.getUserId(), pageable)));
  }

  @PostMapping("/invites/{inviteId}/accept")
  @Operation(
      summary = "초대 수락",
      description = "받은 초대를 수락합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "수락 성공",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "403", description = "권한 없음 (본인 초대 아님)",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "404", description = "초대 없음",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  public ResponseEntity<ResponseDto> acceptInvite(
      @PathVariable Long inviteId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    inviteService.acceptInvite(inviteId, userDetails.getUserId());
    return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS));
  }

  @PostMapping("/invites/{inviteId}/reject")
  @Operation(
      summary = "초대 거절",
      description = "받은 초대를 거절합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "거절 성공",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "403", description = "권한 없음 (본인 초대 아님)",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "404", description = "초대 없음",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  public ResponseEntity<ResponseDto> rejectInvite(
      @PathVariable Long inviteId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    inviteService.rejectInvite(inviteId, userDetails.getUserId());
    return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS));
  }
}
