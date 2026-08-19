package com.muscat.Collabus.Notification.service;

import com.muscat.Collabus.Notification.dto.NotificationResponse;
import com.muscat.Collabus.enums.NotificationType;
import com.muscat.Collabus.common.dto.PageResponseDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

  // 알림을 만든다. 전송은 커밋 이후에 이뤄진다
  void createNotification(Long userId, NotificationType type, String message,
      Long relatedEntityId);

  // 전체 알림 목록. 최신순
  PageResponseDto<NotificationResponse> getUserNotifications(Long userId, Pageable pageable);

  // 미읽음 알림 목록. 최신순
  PageResponseDto<NotificationResponse> getUnreadNotifications(Long userId, Pageable pageable);

  // 미읽음 개수를 센다
  Long getUnreadCount(Long userId);

  // 알림을 읽음으로 바꾼다
  void markAsRead(Long notificationId, Long userId);

  // 미읽음 전부를 읽음으로 바꾼다
  void markAllAsRead(Long userId);

  // 알림을 삭제한다
  void deleteNotification(Long notificationId, Long userId);

  // 최근 알림 N개
  List<NotificationResponse> getRecentNotifications(Long userId, int limit);
}
