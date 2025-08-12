package com.muscat.Collabus.Todo.controller;

import com.muscat.Collabus.Todo.model.TodoRequestDto;
import com.muscat.Collabus.Todo.model.TodoResponseDto;
import com.muscat.Collabus.Todo.service.TodoService;
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
@RequestMapping("/api/todo")
@RequiredArgsConstructor
@Tag(name = "Todo API", description = "할일(Todo) 관리 API")
public class TodoController {

  private final TodoService todoService;

  @PostMapping
  @Operation(
      summary = "Todo 생성",
      description = "Task에 속한 새로운 Todo를 생성합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "생성 성공",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "403", description = "권한 없음",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "404", description = "Task 없음",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  public ResponseEntity<ResponseDto> createTodo(
      @RequestBody TodoRequestDto dto,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
        todoService.createTodo(dto, userDetails.getUserId())));
  }

  @GetMapping
  @Operation(
      summary = "특정 Task의 Todo 목록 조회",
      description = "Task ID와 선택적 상태(status)로 Todo 목록을 조회합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "조회 성공",
              content = @Content(array = @ArraySchema(schema = @Schema(implementation = TodoResponseDto.class)))),
          @ApiResponse(responseCode = "403", description = "권한 없음",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "404", description = "Task 없음",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  public ResponseEntity<ResponseDto> getTodosByTask(
      @RequestParam Long taskId,
      @RequestParam(required = false) String status
  ) {
    return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
        todoService.getTodosByTask(taskId, status)));
  }

  @GetMapping("/{todoId}")
  @Operation(
      summary = "Todo 상세 조회",
      description = "특정 ID의 Todo 단건을 조회합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "조회 성공",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "404", description = "Todo 없음",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  public ResponseEntity<ResponseDto> getTodo(@PathVariable Long todoId) {
    return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
        todoService.getTodoById(todoId)));
  }

  @PatchMapping("/{todoId}")
  @Operation(
      summary = "Todo 수정",
      description = "제목/설명/마감일 등 Todo 정보를 수정합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "수정 성공",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "403", description = "권한 없음",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "404", description = "Todo 없음",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  public ResponseEntity<ResponseDto> updateTodo(
      @PathVariable Long todoId,
      @RequestBody TodoRequestDto dto,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
        todoService.updateTodo(todoId, dto, userDetails.getUserId())));
  }

  @DeleteMapping("/{todoId}")
  @Operation(
      summary = "Todo 삭제",
      description = "Todo를 삭제합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "삭제 성공",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "403", description = "권한 없음",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "404", description = "Todo 없음",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  public ResponseEntity<ResponseDto> deleteTodo(
      @PathVariable Long todoId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    todoService.deleteTodo(todoId, userDetails.getUserId());
    return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS));
  }

  @PatchMapping("/{todoId}/complete")
  @Operation(
      summary = "나의 Todo 완료 처리",
      description = "자신에게 할당된 Todo를 완료 처리합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "완료 처리 성공",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "403", description = "권한 없음",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "404", description = "Todo 또는 User 없음",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  public ResponseEntity<ResponseDto> completeOwnTodo(
      @PathVariable Long todoId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    todoService.completeOwnTodo(todoId, userDetails.getUserId());
    return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS));
  }

  @PatchMapping("/{todoId}/confirm")
  @Operation(
      summary = "Todo 최종 확인 (TaskManager)",
      description = "Task 담당자가 완료된 Todo를 최종 확인합니다.",
      responses = {
          @ApiResponse(
              responseCode = "200", description = "확인 성공",
              content = @Content(schema = @Schema(implementation = TodoResponseDto.class))
          ),
          @ApiResponse(
              responseCode = "403", description = "권한 없음",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))
          ),
          @ApiResponse(
              responseCode = "409", description = "최종 확인 불가 상태",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))
          ),
          @ApiResponse(
              responseCode = "404", description = "Todo 없음",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))
          )
      }
  )
  public ResponseEntity<ResponseDto> confirmTodoCompletion(
      @PathVariable Long todoId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    TodoResponseDto updated = todoService.confirmTodoCompletion(todoId, userDetails.getUserId());
    return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS, updated));
  }

  @PatchMapping("/{todoId}/assignee/{userId}")
  @Operation(
      summary = "담당자 변경",
      description = "Todo의 담당자를 다른 유저로 변경합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "변경 성공", content = @Content),
          @ApiResponse(responseCode = "403", description = "권한 없음",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "404", description = "Todo 또는 User 없음",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  public ResponseEntity<Void> changeAssignee(
      @PathVariable Long todoId,
      @PathVariable Long userId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    todoService.changeAssignee(todoId, userId, userDetails.getUserId());
    return ResponseEntity.ok().build();
  }
}
