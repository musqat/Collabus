package com.muscat.Collabus.Notification.service.impl;

import com.muscat.Collabus.Notification.dto.NotificationResponse;
import com.muscat.Collabus.Notification.entity.Notification;
import com.muscat.Collabus.Notification.repository.NotificationRepository;
import com.muscat.Collabus.Notification.service.NotificationService;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.common.util.EntityFinderUtil;
import com.muscat.Collabus.config.websocket.WebSocketService;
import com.muscat.Collabus.enums.NotificationType;
import com.muscat.Collabus.enums.response.CommonResponse;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final EntityFinderUtil finder;
  private final WebSocketService webSocketService;

  @Transactional
  @Override
  public void createNotification(Long userId, NotificationType type, String message,
      Long relatedEntityId) {
    User user = finder.findUserById(userId);

    Notification notification = Notification.builder()
        .user(user)
        .type(type)
        .message(message)
        .relatedEntityId(relatedEntityId)
        .isRead(false)
        .build();

    Notification savedNotification = notificationRepository.save(notification);

    // WebSocket으로 실시간 알림 전송
    NotificationResponse response = NotificationResponse.from(savedNotification);
    webSocketService.sendNotificationToUser(userId, response);
  }

  @Override
  public List<NotificationResponse> getUserNotifications(Long userId) {
    User user = finder.findUserById(userId);
    List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
    return notifications.stream()
        .map(NotificationResponse::from)
        .collect(Collectors.toList());
  }

  @Override
  public List<NotificationResponse> getUnreadNotifications(Long userId) {
    User user = finder.findUserById(userId);
    List<Notification> notifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(
        user);
    return notifications.stream()
        .map(NotificationResponse::from)
        .collect(Collectors.toList());
  }

  @Override
  public Long getUnreadCount(Long userId) {
    User user = finder.findUserById(userId);
    return notificationRepository.countByUserAndIsReadFalse(user);
  }

  @Transactional
  @Override
  public void markAsRead(Long notificationId, Long userId) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new BusinessException(CommonResponse.RESOURCE_NOT_FOUND));

    // 본인의 알림인지 확인
    if (!notification.getUser().getId().equals(userId)) {
      throw new BusinessException(CommonResponse.FORBIDDEN);
    }

    notification.markAsRead();
  }

  @Transactional
  @Override
  public void markAllAsRead(Long userId) {
    User user = finder.findUserById(userId);
    List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(
        user);

    for (Notification notification : unreadNotifications) {
      notification.markAsRead();
    }
  }

  @Transactional
  @Override
  public void deleteNotification(Long notificationId, Long userId) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new BusinessException(CommonResponse.RESOURCE_NOT_FOUND));

    // 본인의 알림인지 확인
    if (!notification.getUser().getId().equals(userId)) {
      throw new BusinessException(CommonResponse.FORBIDDEN);
    }

    notificationRepository.delete(notification);
  }

  @Override
  public List<NotificationResponse> getRecentNotifications(Long userId, int limit) {
    User user = finder.findUserById(userId);
    List<Notification> notifications = notificationRepository.findRecentNotifications(user,
        PageRequest.of(0, limit));
    return notifications.stream()
        .map(NotificationResponse::from)
        .collect(Collectors.toList());
  }
}
