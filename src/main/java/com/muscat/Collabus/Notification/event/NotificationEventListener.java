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

  /**
   * 커밋 이후에만 전송한다. 커밋 전에 보내면 뒤이어 롤백됐을 때 조회되지 않는 유령 알림이 남는다.
   */
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onNotificationCreated(NotificationCreatedEvent event) {
    try {
      webSocketService.sendNotificationToUser(event.userId(), event.notification());
    } catch (Exception e) {
      // 실시간 전송 실패가 이미 커밋된 비즈니스 트랜잭션에 영향을 주면 안 된다
      log.warn("실시간 알림 전송 실패 - userId={}", event.userId(), e);
    }
  }
}
