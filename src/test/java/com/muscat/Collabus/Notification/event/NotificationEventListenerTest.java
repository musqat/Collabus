package com.muscat.Collabus.Notification.event;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.muscat.Collabus.Notification.dto.NotificationResponse;
import com.muscat.Collabus.config.websocket.WebSocketService;
import com.muscat.Collabus.enums.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("알림 전송 리스너")
class NotificationEventListenerTest {

  private static final Long USER_ID = 7L;

  @Mock
  private WebSocketService webSocketService;

  @InjectMocks
  private NotificationEventListener listener;

  private final NotificationResponse notification = NotificationResponse.builder()
      .id(1L)
      .type(NotificationType.TASK_ASSIGNED)
      .message("태스크가 할당되었습니다")
      .isRead(false)
      .build();

  @Test
  @DisplayName("받은 알림을 그 사용자에게 보낸다")
  void sendsToUser() {
    listener.onNotificationCreated(new NotificationCreatedEvent(USER_ID, notification));

    verify(webSocketService, times(1)).sendNotificationToUser(USER_ID, notification);
  }

  @Test
  @DisplayName("전송이 실패해도 예외를 밖으로 내지 않는다")
  void swallowsSendFailure() {
    doThrow(new IllegalStateException("브로커 연결 끊김"))
        .when(webSocketService).sendNotificationToUser(USER_ID, notification);

    assertThatCode(() ->
        listener.onNotificationCreated(new NotificationCreatedEvent(USER_ID, notification)))
        .doesNotThrowAnyException();
  }
}
