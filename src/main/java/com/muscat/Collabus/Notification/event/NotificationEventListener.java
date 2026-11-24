package com.muscat.Collabus.Notification.event;

import com.muscat.Collabus.config.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private final WebSocketService webSocketService;

  // 커밋 이후에만 사용자에게 전송
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onNotificationCreated(NotificationCreatedEvent event) {
    try {
      webSocketService.sendNotificationToUser(event.userId(), event.notification());
    } catch (Exception e) {
      // 전송 실패는 기록만 하고 흡수
      log.warn("실시간 알림 전송 실패 - userId={}", event.userId(), e);
    }
  }
}
