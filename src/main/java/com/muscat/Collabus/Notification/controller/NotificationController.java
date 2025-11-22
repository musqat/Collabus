package com.muscat.Collabus.Notification.controller;

import com.muscat.Collabus.Notification.dto.NotificationResponse;
import com.muscat.Collabus.Notification.service.NotificationService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification API", description = "알림 관리 API")
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  @Operation(
      summary = "사용자의 모든 알림 조회",
      responses = {
          @ApiResponse(responseCode = "200", description = "성공",
              content = @Content(schema = @Schema(implementation = NotificationResponse.class)))
      }
  )
  public ResponseEntity<ResponseDto> getUserNotifications(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
        notificationService.getUserNotifications(userDetails.getUserId())));
  }

  @GetMapping("/unread")
  @Operation(
      summary = "읽지 않은 알림 조회",
      responses = {
          @ApiResponse(responseCode = "200", description = "성공",
              content = @Content(schema = @Schema(implementation = NotificationResponse.class)))
      }
  )
  public ResponseEntity<ResponseDto> getUnreadNotifications(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
        notificationService.getUnreadNotifications(userDetails.getUserId())));
  }

  @GetMapping("/unread/count")
  @Operation(
      summary = "읽지 않은 알림 개수 조회",
      responses = {
          @ApiResponse(responseCode = "200", description = "성공")
      }
  )
  public ResponseEntity<ResponseDto> getUnreadCount(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
        notificationService.getUnreadCount(userDetails.getUserId())));
  }

  @GetMapping("/recent")
  @Operation(
      summary = "최근 알림 N개 조회",
      responses = {
          @ApiResponse(responseCode = "200", description = "성공",
              content = @Content(schema = @Schema(implementation = NotificationResponse.class)))
      }
  )
  public ResponseEntity<ResponseDto> getRecentNotifications(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(defaultValue = "10") int limit) {
    return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
        notificationService.getRecentNotifications(userDetails.getUserId(), limit)));
  }

  @PatchMapping("/{notificationId}/read")
  @Operation(
      summary = "알림 읽음 처리",
      responses = {
          @ApiResponse(responseCode = "200", description = "성공"),
          @ApiResponse(responseCode = "403", description = "권한 없음",
              content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
          @ApiResponse(responseCode = "404", description = "알림 없음",
              content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
      }
  )
  public ResponseEntity<ResponseDto> markAsRead(
      @PathVariable Long notificationId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    notificationService.markAsRead(notificationId, userDetails.getUserId());
    return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS));
  }

  @PatchMapping("/read-all")
  @Operation(
      summary = "모든 알림 읽음 처리",
      responses = {
          @ApiResponse(responseCode = "200", description = "성공")
      }
  )
  public ResponseEntity<ResponseDto> markAllAsRead(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    notificationService.markAllAsRead(userDetails.getUserId());
    return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS));
  }

  @DeleteMapping("/{notificationId}")
  @Operation(
      summary = "알림 삭제",
      responses = {
          @ApiResponse(responseCode = "200", description = "성공"),
          @ApiResponse(responseCode = "403", description = "권한 없음",
              content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
          @ApiResponse(responseCode = "404", description = "알림 없음",
              content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
      }
  )
  public ResponseEntity<ResponseDto> deleteNotification(
      @PathVariable Long notificationId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    notificationService.deleteNotification(notificationId, userDetails.getUserId());
    return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS));
  }
}
