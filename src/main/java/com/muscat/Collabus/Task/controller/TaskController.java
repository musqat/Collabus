package com.muscat.Collabus.Task.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import jakarta.validation.Valid;
import com.muscat.Collabus.Task.model.TaskRequestDto;
import com.muscat.Collabus.Task.model.TaskResponseDto;
import com.muscat.Collabus.Task.model.TaskUpdateRequestDto;
import com.muscat.Collabus.Task.model.TaskUserResponseDto;
import com.muscat.Collabus.Task.service.TaskService;
import com.muscat.Collabus.common.dto.ErrorResponseDto;
import com.muscat.Collabus.common.dto.ResponseDto;
import com.muscat.Collabus.config.security.CustomUserDetails;
import com.muscat.Collabus.enums.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Task API", description = "태스크(Task) 관리 API")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(
            summary = "Task 생성",
            description = """
                    - Workspace Master(WM)만 생성 가능
                    - 생성 시 요청자를 Task Manager(TM)로 자동 지정
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공",
                            content = @Content(schema = @Schema(implementation = TaskResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "권한 없음 (WM이 아님)",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "워크스페이스 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
            }
    )
    public ResponseEntity<ResponseDto> createTask(
            @RequestBody @Valid TaskRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
                taskService.createTask(dto, userDetails.getUserId())));
    }

    @GetMapping("/{taskId}")
    @Operation(
            summary = "Task 단건 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공",
                            content = @Content(schema = @Schema(implementation = TaskResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Task 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
            }
    )
    public ResponseEntity<ResponseDto> getTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
                taskService.getTask(taskId)));
    }

    @GetMapping("/workspaces/{workspaceId}/tasks")
    @Operation(
            summary = "워크스페이스 내 Task 전체 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공",
                            content = @Content(schema = @Schema(implementation = TaskResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "워크스페이스 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
            }
    )
    public ResponseEntity<ResponseDto> getTasksByWorkspace(@PathVariable Long workspaceId,
                                                           @PageableDefault(size = 20, sort = "dueDate") Pageable pageable) {
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
                taskService.getTasksByWorkspace(workspaceId, pageable)));
    }

    @PatchMapping("/{taskId}")
    @Operation(
            summary = "Task 수정",
            description = "Task Manager(TM) 또는 Workspace Master(WM)만 수정 가능",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공",
                            content = @Content(schema = @Schema(implementation = TaskResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "권한 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Task 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
            }
    )
    public ResponseEntity<ResponseDto> updateTask(
            @PathVariable Long taskId,
            @RequestBody @Valid TaskUpdateRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
                taskService.updateTask(taskId, dto, userDetails.getUserId())));
    }

    @DeleteMapping("/{taskId}")
    @Operation(
            summary = "Task 삭제",
            description = "Workspace Master(WM) 또는 Task Manager(TM)만 삭제 가능",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "권한 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Task 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
            }
    )
    public ResponseEntity<ResponseDto> deleteTask(@PathVariable Long taskId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        taskService.deleteTask(taskId, userDetails.getUserId());
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS));
    }

    @PostMapping("/{taskId}/members")
    @Operation(
            summary = "Task에 유저 추가",
            description = "Workspace Master(WM)만 가능",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "권한 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Task 또는 User 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                    @ApiResponse(responseCode = "409", description = "이미 Task에 속한 유저",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
            }
    )
    public ResponseEntity<ResponseDto> assignUser(
            @PathVariable Long taskId,
            @RequestParam Long targetUserId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        taskService.assignUserToTask(taskId, targetUserId, userDetails.getUserId());
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS));
    }

    @DeleteMapping("/{taskId}/members/{userId}")
    @Operation(
            summary = "Task에서 유저 제거",
            description = "WM은 누구나 제거 가능, TM은 자기 자신만 제거 불가",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "권한 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Task 또는 TaskUser 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                    @ApiResponse(responseCode = "409", description = "본인 제거 불가 (TM)",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
            }
    )
    public ResponseEntity<ResponseDto> removeUser(
            @PathVariable Long taskId,
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        taskService.removeUserFromTask(taskId, userId, userDetails.getUserId());
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS));
    }

    @GetMapping("/{taskId}/members")
    @Operation(
            summary = "Task 참여자 목록 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공",
                            content = @Content(schema = @Schema(implementation = TaskUserResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Task 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
            }
    )
    public ResponseEntity<ResponseDto> getMembers(@PathVariable Long taskId) {
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
                taskService.getTaskMembers(taskId)));
    }

    @PatchMapping("/{taskId}/manager")
    @Operation(
            summary = "Task Manager(TM) 지정",
            description = "Workspace Master(WM)만 지정 가능, 대상 유저는 Task에 속해 있어야 함",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "권한 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Task 또는 지정 유저 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                    @ApiResponse(responseCode = "409", description = "대상 유저가 Task에 속하지 않음",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
            }
    )
    public ResponseEntity<ResponseDto> assignTaskManager(
            @PathVariable Long taskId,
            @RequestParam Long newManagerId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        taskService.assignTaskManager(taskId, newManagerId, userDetails.getUserId());
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS));
    }
}
