package com.muscat.Collabus.Workspace.controller;

import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Pageable;
import com.muscat.Collabus.common.dto.PageResponseDto;
import com.muscat.Collabus.Workspace.model.WorkspaceRequestDto;
import com.muscat.Collabus.Workspace.model.WorkspaceResponseDto;
import com.muscat.Collabus.Workspace.service.WorkspaceService;
import com.muscat.Collabus.common.dto.ErrorResponseDto;
import com.muscat.Collabus.common.dto.ResponseDto;
import com.muscat.Collabus.enums.response.CommonResponse;
import com.muscat.Collabus.config.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import org.springdoc.core.annotations.ParameterObject;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
@Tag(name = "Workspace API", description = "워크스페이스 관련 API")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @Operation(
            summary = "워크스페이스 생성",
            description = "워크스페이스를 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "생성 성공",
                            content = @Content(schema = @Schema(implementation = WorkspaceResponseDto.class))),
                    @ApiResponse(responseCode = "409", description = "이미 존재하는 워크스페이스",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
            }
    )
    @PostMapping
    public ResponseEntity<ResponseDto> createWorkspace(
            @RequestBody @Valid WorkspaceRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long founderId = userDetails.getUserId();
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
                workspaceService.createWorkspace(dto, founderId)));
    }

    @Operation(
            summary = "워크스페이스 단건 조회",
            description = "워크스페이스 ID를 기반으로 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = WorkspaceResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "워크스페이스를 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto> getWorkspaceById(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
                workspaceService.getWorkspaceById(id, userDetails.getUserId())));
    }

    @Operation(
            summary = "참여 중인 워크스페이스 목록 조회",
            description = "내가 참여 중인 모든 워크스페이스 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = WorkspaceResponseDto.class))))
            }
    )
    @GetMapping("/joined")
    public ResponseEntity<ResponseDto> getJoinedWorkspaces(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
                workspaceService.getJoinedWorkspaces(userDetails.getUserId(), pageable)));
    }

    @Operation(
            summary = "워크스페이스 수정",
            description = "워크스페이스 정보를 수정합니다. (권한 필요)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공",
                            content = @Content(schema = @Schema(implementation = WorkspaceResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "권한 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "워크스페이스 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto> updateWorkspace(
            @PathVariable Long id,
            @RequestBody @Valid WorkspaceRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
                workspaceService.updateWorkspace(id, dto, userDetails.getUserId())));
    }

    @Operation(
            summary = "워크스페이스 삭제",
            description = "워크스페이스를 삭제합니다. (권한 필요)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "권한 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "워크스페이스 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto> deleteWorkspace(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        workspaceService.deleteWorkspace(id, userDetails.getUserId());
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS));
    }
}
