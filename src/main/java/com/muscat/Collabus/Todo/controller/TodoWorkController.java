package com.muscat.Collabus.Todo.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import jakarta.validation.Valid;
import com.muscat.Collabus.Todo.model.TodoWorkDto;
import com.muscat.Collabus.Todo.service.TodoWorkService;
import com.muscat.Collabus.common.dto.ResponseDto;
import com.muscat.Collabus.config.security.CustomUserDetails;
import com.muscat.Collabus.enums.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/todo/works")
@RequiredArgsConstructor
@Tag(name = "Todo Work API", description = "Todo 산출물/작업 기록 관리 API")
public class TodoWorkController {

    private final TodoWorkService todoWorkService;

    @PostMapping
    @Operation(
            summary = "Work 등록",
            description = "특정 Todo에 작업 기록(Work)을 등록합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "등록 성공",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "권한 없음",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Todo 없음",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class)))
            }
    )
    public ResponseEntity<ResponseDto> createWork(
            @RequestParam Long todoId,
            @RequestBody @Valid TodoWorkDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TodoWorkDto saved = todoWorkService.createWork(todoId, dto, userDetails.getUserId());
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS, saved));
    }

    @GetMapping
    @Operation(
            summary = "Work 목록 조회",
            description = "특정 Todo의 작업 기록(Work) 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TodoWorkDto.class)))),
                    @ApiResponse(responseCode = "404", description = "Todo 없음",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class)))
            }
    )
    public ResponseEntity<ResponseDto> getWorks(@RequestParam Long todoId,
                                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                                @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
                todoWorkService.getWorksByTodoId(todoId, userDetails.getUserId(), pageable)));
    }

    @PatchMapping("/{workId}")
    @Operation(
            summary = "Work 수정",
            description = "등록한 작업 기록(Work)을 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "작성자가 아님",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Work 없음",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class)))
            }
    )
    public ResponseEntity<ResponseDto> updateWork(
            @PathVariable Long workId,
            @RequestBody @Valid TodoWorkDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TodoWorkDto updated = todoWorkService.updateWork(workId, dto, userDetails.getUserId());
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS, updated));
    }

    @DeleteMapping("/{workId}")
    @Operation(
            summary = "Work 삭제",
            description = "등록한 작업 기록(Work)을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "작성자가 아님",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Work 없음",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class)))
            }
    )
    public ResponseEntity<ResponseDto> deleteWork(
            @PathVariable Long workId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        todoWorkService.deleteWork(workId, userDetails.getUserId());
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS));
    }
}
