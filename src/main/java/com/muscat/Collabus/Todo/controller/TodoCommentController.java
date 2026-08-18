package com.muscat.Collabus.Todo.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import jakarta.validation.Valid;
import com.muscat.Collabus.Todo.model.TodoCommentDto;
import com.muscat.Collabus.Todo.service.TodoCommentService;
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
@RequestMapping("/api/todo/comments")
@RequiredArgsConstructor
@Tag(name = "Todo Comment API", description = "Todo 댓글 관리 API")
public class TodoCommentController {

    private final TodoCommentService todoCommentService;

    @PostMapping
    @Operation(
            summary = "댓글 작성",
            description = "Todo에 댓글을 작성합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "작성 성공",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "권한 없음",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Todo 없음",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class)))
            }
    )
    public ResponseEntity<ResponseDto> createComment(
            @RequestParam Long todoId,
            @RequestBody @Valid TodoCommentDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TodoCommentDto saved = todoCommentService.addComment(
                todoId,
                dto.getContent(),
                userDetails.getUserId()
        );
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS, saved));
    }

    @GetMapping
    @Operation(
            summary = "댓글 목록 조회",
            description = "특정 Todo의 댓글 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TodoCommentDto.class)))),
                    @ApiResponse(responseCode = "404", description = "Todo 없음",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class)))
            }
    )
    public ResponseEntity<ResponseDto> getComments(@RequestParam Long todoId,
                                                   @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
                todoCommentService.getComments(todoId, pageable)));
    }

    @PatchMapping("/{commentId}")
    @Operation(
            summary = "댓글 수정",
            description = "자신이 작성한 댓글을 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "작성자가 아님",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "댓글 없음",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class)))
            }
    )
    public ResponseEntity<ResponseDto> updateComment(
            @PathVariable Long commentId,
            @RequestBody @Valid TodoCommentDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TodoCommentDto updated = todoCommentService.updateComment(
                commentId,
                dto.getContent(),
                userDetails.getUserId()
        );
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS, updated));
    }

    @DeleteMapping("/{commentId}")
    @Operation(
            summary = "댓글 삭제",
            description = "자신이 작성한 댓글을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "작성자가 아님",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "댓글 없음",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class)))
            }
    )
    public ResponseEntity<ResponseDto> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        todoCommentService.deleteComment(commentId, userDetails.getUserId());
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS));
    }
}
