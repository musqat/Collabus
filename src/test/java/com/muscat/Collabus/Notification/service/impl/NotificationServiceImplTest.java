package com.muscat.Collabus.Notification.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.muscat.Collabus.Notification.entity.Notification;
import com.muscat.Collabus.Notification.event.NotificationCreatedEvent;
import com.muscat.Collabus.Notification.repository.NotificationRepository;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.common.util.EntityFinderUtil;
import com.muscat.Collabus.common.util.SortGuard;
import com.muscat.Collabus.enums.NotificationType;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("알림")
class NotificationServiceImplTest {

  private static final Long OWNER_ID = 1L;
  private static final Long OUTSIDER_ID = 99L;
  private static final Long NOTIFICATION_ID = 5L;

  @Mock
  private SortGuard sortGuard;

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private EntityFinderUtil finder;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private NotificationServiceImpl notificationService;

  private User owner;

  @BeforeEach
  void setUp() {
    owner = User.builder().id(OWNER_ID).build();
  }

  private Notification ownedNotification() {
    return Notification.builder()
        .user(owner)
        .type(NotificationType.COMMENT_ADDED)
        .message("m")
        .isRead(false)
        .build();
  }

  @Test
  @DisplayName("알림을 만들면 전송 이벤트를 발행한다")
  void createNotification_PublishesEvent() {
    when(finder.findUserById(OWNER_ID)).thenReturn(owner);
    when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));
    when(notificationRepository.findByUser_IdOrderByIdDesc(anyLong(), any(Pageable.class)))
        .thenReturn(List.of());

    notificationService.createNotification(OWNER_ID, NotificationType.COMMENT_ADDED, "m", 1L);

    verify(eventPublisher, times(1)).publishEvent(any(NotificationCreatedEvent.class));
  }

  @Test
  @DisplayName("보관 개수를 넘지 않으면 오래된 알림을 지우지 않는다")
  void createNotification_KeepsWhenUnderLimit() {
    when(finder.findUserById(OWNER_ID)).thenReturn(owner);
    when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));
    when(notificationRepository.findByUser_IdOrderByIdDesc(anyLong(), any(Pageable.class)))
        .thenReturn(List.of());

    notificationService.createNotification(OWNER_ID, NotificationType.COMMENT_ADDED, "m", 1L);

    verify(notificationRepository, never()).deleteByUser_IdAndIdLessThanEqual(anyLong(), anyLong());
  }

  @Test
  @DisplayName("보관 개수를 넘기면 경계보다 오래된 알림을 지운다")
  void createNotification_TrimsOldOnes() {
    Notification boundary = ownedNotification();
    when(finder.findUserById(OWNER_ID)).thenReturn(owner);
    when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));
    when(notificationRepository.findByUser_IdOrderByIdDesc(anyLong(), any(Pageable.class)))
        .thenReturn(List.of(boundary));

    notificationService.createNotification(OWNER_ID, NotificationType.COMMENT_ADDED, "m", 1L);

    verify(notificationRepository, times(1))
        .deleteByUser_IdAndIdLessThanEqual(anyLong(), any());
  }

  @Test
  @DisplayName("본인 알림만 읽음 처리할 수 있다")
  void markAsRead_Fail_NotOwner() {
    when(notificationRepository.findById(NOTIFICATION_ID))
        .thenReturn(Optional.of(ownedNotification()));

    assertThatThrownBy(() -> notificationService.markAsRead(NOTIFICATION_ID, OUTSIDER_ID))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("본인 알림은 읽음으로 바뀐다")
  void markAsRead_Success() {
    Notification notification = ownedNotification();
    when(notificationRepository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(notification));

    notificationService.markAsRead(NOTIFICATION_ID, OWNER_ID);

    assertThat(notification.getIsRead()).isTrue();
  }

  @Test
  @DisplayName("없는 알림은 읽음 처리할 수 없다")
  void markAsRead_Fail_NotFound() {
    when(notificationRepository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> notificationService.markAsRead(NOTIFICATION_ID, OWNER_ID))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("본인 알림만 삭제할 수 있다")
  void deleteNotification_Fail_NotOwner() {
    when(notificationRepository.findById(NOTIFICATION_ID))
        .thenReturn(Optional.of(ownedNotification()));

    assertThatThrownBy(() -> notificationService.deleteNotification(NOTIFICATION_ID, OUTSIDER_ID))
        .isInstanceOf(BusinessException.class);

    verify(notificationRepository, never()).delete(any());
  }

  @Test
  @DisplayName("본인 알림은 지울 수 있다")
  void deleteNotification_Success() {
    Notification notification = ownedNotification();
    when(notificationRepository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(notification));

    notificationService.deleteNotification(NOTIFICATION_ID, OWNER_ID);

    verify(notificationRepository, times(1)).delete(notification);
  }

  @Test
  @DisplayName("미읽음 전부를 읽음으로 바꾼다")
  void markAllAsRead() {
    Notification first = ownedNotification();
    Notification second = ownedNotification();
    when(finder.findUserById(OWNER_ID)).thenReturn(owner);
    when(notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(owner))
        .thenReturn(List.of(first, second));

    notificationService.markAllAsRead(OWNER_ID);

    assertThat(first.getIsRead()).isTrue();
    assertThat(second.getIsRead()).isTrue();
  }
}
