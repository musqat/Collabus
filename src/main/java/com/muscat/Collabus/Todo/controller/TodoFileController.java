package com.muscat.Collabus.Todo.controller;

import com.muscat.Collabus.Todo.model.TodoFileDto;
import com.muscat.Collabus.Todo.service.TodoFileService;
import com.muscat.Collabus.common.dto.ResponseDto;
import com.muscat.Collabus.config.security.CustomUserDetails;
import com.muscat.Collabus.enums.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.nio.charset.StandardCharsets;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/todo/files")
@RequiredArgsConstructor
@Tag(name = "TodoFile API", description = "Todo 작업에 첨부된 파일 관리 API")
public class TodoFileController {

    private final TodoFileService todoFileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "파일 업로드",
            description = "특정 TodoWork에 파일을 첨부합니다. 해당 Task 참여자만 업로드할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업로드 성공",
                    content = @Content(schema = @Schema(implementation = TodoFileDto.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "Work 또는 User 없음")
    })
    public ResponseEntity<ResponseDto> uploadFile(
            @RequestParam Long workId,
            @RequestPart MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
                todoFileService.createFile(workId, userDetails.getUserId(), file)));
    }

    @GetMapping("/work/{workId}")
    @Operation(
            summary = "TodoWork의 파일 목록 조회",
            description = "특정 작업에 첨부된 모든 파일을 조회합니다. 해당 Task 참여자만 조회할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = TodoFileDto.class))),
            @ApiResponse(responseCode = "403", description = "Task 참여자 아님"),
            @ApiResponse(responseCode = "404", description = "Work 없음")
    })
    public ResponseEntity<ResponseDto> getFilesByWork(
            @PathVariable Long workId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
                todoFileService.getFilesByWorkId(workId, userDetails.getUserId())));
    }

    @GetMapping("/{fileId}/download")
    @Operation(
            summary = "파일 다운로드",
            description = "첨부 파일을 내려받습니다. 해당 Task 참여자만 다운로드할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "다운로드 성공"),
            @ApiResponse(responseCode = "403", description = "Task 참여자 아님"),
            @ApiResponse(responseCode = "404", description = "파일 없음")
    })
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long fileId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TodoFileService.DownloadedFile downloaded =
                todoFileService.downloadFile(fileId, userDetails.getUserId());

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(downloaded.originalName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(downloaded.resource());
    }

    @PatchMapping(value = "/{fileId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "파일 교체 (수정)",
            description = "기존 업로드된 파일을 새 파일로 교체합니다. 업로더 본인만 가능합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = TodoFileDto.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "파일 없음")
    })
    public ResponseEntity<ResponseDto> updateFile(
            @PathVariable Long fileId,
            @RequestPart MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
                todoFileService.updateFile(fileId, userDetails.getUserId(), file)));
    }

    @DeleteMapping("/{fileId}")
    @Operation(
            summary = "파일 삭제",
            description = "업로더 본인만 첨부된 파일을 삭제할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공",
                    content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "파일 없음")
    })
    public ResponseEntity<ResponseDto> deleteFile(
            @PathVariable Long fileId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        todoFileService.deleteFile(fileId, userDetails.getUserId());
        return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS));
    }
}
