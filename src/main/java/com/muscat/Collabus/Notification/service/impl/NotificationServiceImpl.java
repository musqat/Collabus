package com.muscat.Collabus.Notification.service.impl;

import com.muscat.Collabus.Notification.dto.NotificationResponse;
import com.muscat.Collabus.Notification.entity.Notification;
import com.muscat.Collabus.Notification.repository.NotificationRepository;
import com.muscat.Collabus.Notification.service.NotificationService;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.common.util.EntityFinderUtil;
import com.muscat.Collabus.Notification.event.NotificationCreatedEvent;
import com.muscat.Collabus.enums.NotificationType;
import com.muscat.Collabus.enums.response.CommonResponse;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import com.muscat.Collabus.common.dto.PageResponseDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final EntityFinderUtil finder;
  private final ApplicationEventPublisher eventPublisher;

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

    // 실제 전송은 트랜잭션 커밋 이후 NotificationEventListener 가 수행한다
    eventPublisher.publishEvent(
        new NotificationCreatedEvent(userId, NotificationResponse.from(savedNotification)));
  }

  @Override
  public PageResponseDto<NotificationResponse> getUserNotifications(Long userId,
      Pageable pageable) {
    User user = finder.findUserById(userId);
    return PageResponseDto.of(
        notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable),
        NotificationResponse::from);
  }

  @Override
  public PageResponseDto<NotificationResponse> getUnreadNotifications(Long userId,
      Pageable pageable) {
    User user = finder.findUserById(userId);
    return PageResponseDto.of(
        notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user, pageable),
        NotificationResponse::from);
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
    // 최근 N개만 필요하므로 첫 페이지를 limit 크기로 요청한다
    return notificationRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(0, limit))
        .getContent().stream()
        .map(NotificationResponse::from)
        .collect(Collectors.toList());
  }
}
