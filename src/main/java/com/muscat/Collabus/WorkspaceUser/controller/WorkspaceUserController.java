package com.muscat.Collabus.WorkspaceUser.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workspaces")
@Tag(name = "Workspace User API", description = "워크스페이스 멤버 관리 API")
public class WorkspaceUserController {

    private final WorkspaceUserService workspaceUserService;

    @GetMapping("/{workspaceId}/users")
    @Operation(
            summary = "워크스페이스 멤버 목록",
            description = "해당 워크스페이스에 속한 모든 멤버를 반환합니다. (워크스페이스 참여자만 조회 가능)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음 (비참여자)",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "워크스페이스 없음",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class)))
            }
    )
    public ResponseEntity<ResponseDto> getUsersInWorkspace(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
                workspaceUserService.getUsersInWorkspace(workspaceId, userDetails.getUserId(), pageable)));
    }

    @PutMapping("/{workspaceId}/users/{targetUserId}/role")
    @Operation(
            summary = "멤버 역할 변경",
            description = """
                    Workspace MASTER만 변경할 수 있습니다.
                    - 본인 역할은 변경할 수 없습니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "변경 성공",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "권한 없음 또는 자기 자신 변경 시도",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "워크스페이스 또는 대상 유저 없음",
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
            summary = "멤버 제거",
            description = """
                    Workspace MASTER만 제거할 수 있습니다.
                    - 본인은 제거할 수 없습니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "제거 성공",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "권한 없음 또는 자기 자신 제거 시도",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "워크스페이스 또는 대상 유저 없음",
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
                    워크스페이스에서 탈퇴합니다.
                    - 마지막 1인이면 워크스페이스가 삭제됩니다.
                    - MASTER가 나갈 경우 다음 멤버에게 MASTER 권한이 위임됩니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "나가기 성공",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음 (비참여자)",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "워크스페이스 또는 사용자 없음",
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
