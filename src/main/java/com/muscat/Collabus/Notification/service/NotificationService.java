package com.muscat.Collabus.Notification.service;

import com.muscat.Collabus.Notification.dto.NotificationResponse;
import com.muscat.Collabus.enums.NotificationType;
import com.muscat.Collabus.common.dto.PageResponseDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

  // 알림 생성
  void createNotification(Long userId, NotificationType type, String message,
      Long relatedEntityId);

  // 사용자의 모든 알림 조회
  PageResponseDto<NotificationResponse> getUserNotifications(Long userId, Pageable pageable);

  // 사용자의 읽지 않은 알림 조회
  PageResponseDto<NotificationResponse> getUnreadNotifications(Long userId, Pageable pageable);

  // 읽지 않은 알림 개수 조회
  Long getUnreadCount(Long userId);

  // 알림 읽음 처리
  void markAsRead(Long notificationId, Long userId);

  // 모든 알림 읽음 처리
  void markAllAsRead(Long userId);

  // 알림 삭제
  void deleteNotification(Long notificationId, Long userId);

  // 최근 알림 N개 조회
  List<NotificationResponse> getRecentNotifications(Long userId, int limit);
}
