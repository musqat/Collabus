package com.muscat.Collabus.WorkspaceUser.controller;

import com.muscat.Collabus.WorkspaceUser.model.InviteRequestDto;
import com.muscat.Collabus.WorkspaceUser.service.WorkspaceUserInviteService;
import com.muscat.Collabus.common.dto.ResponseDto;
import com.muscat.Collabus.config.security.CustomUserDetails;
import com.muscat.Collabus.enums.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workspaces")
@Tag(name = "Workspace Invite API", description = "워크스페이스 초대 관련 API")
public class WorkspaceUserInviteController {

  private final WorkspaceUserInviteService inviteService;

  @PostMapping("/{workspaceId}/invites")
  @Operation(
      summary = "워크스페이스 사용자 초대",
      description = "워크스페이스에 유저를 초대합니다. 초대는 userId를 기준으로 이루어집니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "초대 성공",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "400", description = "자기 자신을 초대하거나 이미 초대됨",
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
      summary = "내 초대 목록 조회",
      description = "현재 로그인한 유저가 받은 모든 초대 목록을 조회합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "조회 성공",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  public ResponseEntity<ResponseDto> getMyInvites(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(
        new ResponseDto(CommonResponse.SUCCESS, inviteService.getMyInvites(userDetails.getUserId()))
    );
  }

  @PostMapping("/invites/{inviteId}/accept")
  @Operation(
      summary = "초대 수락",
      description = "현재 로그인한 유저가 받은 초대를 수락합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "수락 성공",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "403", description = "권한 없음",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "404", description = "초대 내역 없음",
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
      description = "현재 로그인한 유저가 받은 초대를 거절합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "거절 성공",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "403", description = "권한 없음",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "404", description = "초대 내역 없음",
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
