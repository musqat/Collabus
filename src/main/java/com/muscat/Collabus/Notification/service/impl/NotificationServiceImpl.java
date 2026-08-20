package com.muscat.Collabus.Notification.service.impl;

import com.muscat.Collabus.common.util.SortGuard;
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

    // 사용자당 보관할 최대 알림 수
    private static final int KEEP_PER_USER = 500;

    private final SortGuard sortGuard;

    private final NotificationRepository notificationRepository;
    private final EntityFinderUtil finder;
    private final ApplicationEventPublisher eventPublisher;

    // 전송은 커밋 이후 리스너가 맡는다. 보관 개수를 넘긴 오래된 알림은 여기서 지운다
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

        trimOldNotifications(userId);

        // 실제 전송은 트랜잭션 커밋 이후 NotificationEventListener 가 수행한다
        eventPublisher.publishEvent(
                new NotificationCreatedEvent(userId, NotificationResponse.from(savedNotification)));
    }

    // 사용자당 보관 개수를 넘긴 오래된 알림을 지운다.
    private void trimOldNotifications(Long userId) {
        List<Notification> boundary = notificationRepository.findByUser_IdOrderByIdDesc(
                userId, PageRequest.of(KEEP_PER_USER, 1));

        if (boundary.isEmpty()) {
            return;
        }
        notificationRepository.deleteByUser_IdAndIdLessThanEqual(userId, boundary.getFirst().getId());
    }

    @Override
    public PageResponseDto<NotificationResponse> getUserNotifications(Long userId,
                                                                      Pageable pageable) {
        User user = finder.findUserById(userId);
        return PageResponseDto.of(
                notificationRepository.findByUserOrderByCreatedAtDesc(user,
                        sortGuard.apply(pageable, Notification.class)),
                NotificationResponse::from);
    }

    @Override
    public PageResponseDto<NotificationResponse> getUnreadNotifications(Long userId,
                                                                        Pageable pageable) {
        User user = finder.findUserById(userId);
        return PageResponseDto.of(
                notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user,
                        sortGuard.apply(pageable, Notification.class)),
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

    // 최근 N개. 첫 페이지를 limit 크기로 요청한다
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
